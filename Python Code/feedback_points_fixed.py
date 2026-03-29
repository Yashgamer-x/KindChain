# ============================================================
# GuardAI — Points Add & Deduct Algorithm  (FIXED VERSION)
# Based on star rating + written feedback
#
# BUGS FIXED:
#   1. Tier demotion now correctly labeled "demoted from" vs "promoted from"
#   2. Action label handles total=0 as "UNCHANGED" (not "ADDED")
#   3. Dead threshold key 10:0 removed from FEEDBACK_BONUS
#   4. Dead return "New" removed from get_tier()
#   5. Duplicate review guard added to apply_review()
# ============================================================

from datetime import datetime

# ============================================================
# POINTS RULES
# ============================================================

POINTS_MAP = {
    1: -10,   # 1 star  → deduct 10 points
    2:  -5,   # 2 stars → deduct 5 points
    3:   5,   # 3 stars → add 5 points
    4:  10,   # 4 stars → add 10 points
    5:  20,   # 5 stars → add 20 points
}

# FIX 3: Removed dead key 10:0 — feedback under 50 chars naturally returns bonus=0
FEEDBACK_BONUS = {
    100: 5,   # 100+ chars → +5 bonus
    50:  2,   # 50+ chars  → +2 bonus
}

TIER_THRESHOLDS = [
    (200, "Expert"),
    (100, "Senior"),
    (40,  "Member"),
    (0,   "New"),
]

# Used for tier direction detection (Bug Fix 1)
TIER_ORDER = ["New", "Member", "Senior", "Expert"]


# ============================================================
# CORE ALGORITHM
# ============================================================

def calculate_points(stars, feedback=""):
    """
    Returns (base, bonus, total) points.
    1-2 stars = deduct | 3-5 stars = add
    Bonus only applies for 3+ star reviews with meaningful feedback.
    """
    if stars not in POINTS_MAP:
        raise ValueError("Stars must be between 1 and 5.")

    base = POINTS_MAP[stars]

    bonus = 0
    if stars >= 3 and feedback.strip():
        length = len(feedback.strip())
        for threshold in sorted(FEEDBACK_BONUS.keys(), reverse=True):
            if length >= threshold:
                bonus = FEEDBACK_BONUS[threshold]
                break

    return base, bonus, base + bonus


def get_tier(points):
    # FIX 4: Removed unreachable `return "New"` — TIER_THRESHOLDS already
    # contains (0, "New") as the guaranteed fallback.
    for threshold, tier in TIER_THRESHOLDS:
        if points >= threshold:
            return tier
    # TIER_THRESHOLDS must always include a (0, ...) entry as the final item.


def apply_review(helper, stars, feedback, seeker_name="Anonymous"):
    """
    Apply a review to a helper.
    Adds or deducts points based on stars + feedback quality.
    Automatically updates tier, detects promotion vs demotion.
    Rejects duplicate reviews from the same seeker.
    """
    # FIX 5: Duplicate review guard
    already_reviewed = any(
        r["seeker"].lower() == seeker_name.lower()
        for r in helper["reviews"]
    )
    if already_reviewed:
        print(f"\n  ⚠ {seeker_name} has already reviewed {helper['name']}. Skipping.")
        return None

    base, bonus, total = calculate_points(stars, feedback)

    points_before = helper["points"]
    tier_before   = get_tier(points_before)

    # Apply points — floor at 0 (points never go negative)
    helper["points"] = max(0, helper["points"] + total)
    helper["reviews"].append({
        "seeker"   : seeker_name,
        "stars"    : stars,
        "feedback" : feedback,
        "base"     : base,
        "bonus"    : bonus,
        "total"    : total,
        "before"   : points_before,
        "after"    : helper["points"],
        "time"     : datetime.now().strftime("%Y-%m-%d %H:%M:%S"),
    })

    points_after = helper["points"]
    tier_after   = get_tier(points_after)
    tier_changed = tier_before != tier_after

    # FIX 1: Correctly label promotion vs demotion
    if tier_changed:
        promoted = TIER_ORDER.index(tier_after) > TIER_ORDER.index(tier_before)
        change_label = "promoted from" if promoted else "demoted from"
    else:
        change_label = ""

    # FIX 2: Handle total=0 as "UNCHANGED" — not "ADDED"
    if total > 0:
        action = "ADDED"
        sign   = "+"
    elif total < 0:
        action = "DEDUCTED"
        sign   = ""
    else:
        action = "UNCHANGED"
        sign   = "+"

    print(f"\n  {'─'*52}")
    print(f"  Helper     : {helper['name']}")
    print(f"  Reviewer   : {seeker_name}")
    print(f"  Stars      : {stars}/5  — {get_star_label(stars)}")
    print(f"  Feedback   : {feedback[:60]}{'...' if len(feedback) > 60 else ''}")
    print(f"  {'─'*52}")
    print(f"  Base pts   : {'+' if base >= 0 else ''}{base}")
    print(f"  Bonus pts  : +{bonus}  (feedback length reward)")
    print(f"  {'─'*52}")
    print(f"  Points {action:<10}: {sign}{total}")
    print(f"  Balance    : {points_before}  ->  {points_after}")
    print(f"  Tier       : {tier_after}{(' (' + change_label + ' ' + tier_before + ')') if tier_changed else ''}")
    print(f"  {'─'*52}")

    if tier_changed:
        direction = "🎉 PROMOTED" if promoted else "⚠ DEMOTED"
        print(f"\n  *** {direction}: {tier_before} -> {tier_after} ***\n")

    return {
        "base"         : base,
        "bonus"        : bonus,
        "total"        : total,
        "points_before": points_before,
        "points_after" : points_after,
        "tier_before"  : tier_before,
        "tier_after"   : tier_after,
        "tier_changed" : tier_changed,
    }


def get_star_label(stars):
    return {1: "Poor", 2: "Fair", 3: "Good", 4: "Very Good", 5: "Excellent"}.get(stars, "")


def show_helper_summary(helper):
    reviews   = helper["reviews"]
    total     = len(reviews)
    avg       = round(sum(r["stars"] for r in reviews) / total, 2) if total else 0
    tier      = get_tier(helper["points"])
    breakdown = {1: 0, 2: 0, 3: 0, 4: 0, 5: 0}
    for r in reviews:
        breakdown[r["stars"]] += 1

    print(f"\n  {'═'*50}")
    print(f"  {helper['name'].upper()}")
    print(f"  {'═'*50}")
    print(f"  Points      : {helper['points']}  ({tier})")
    print(f"  Avg Rating  : {avg}/5.0")
    print(f"  Reviews     : {total}")
    print(f"\n  Star Breakdown:")
    for s in range(5, 0, -1):
        count = breakdown[s]
        pct   = round(count / total * 100) if total else 0
        bar   = "█" * (pct // 5) + "░" * (20 - pct // 5)
        pts   = f"({'+' if POINTS_MAP[s] >= 0 else ''}{POINTS_MAP[s]} pts each)"
        print(f"    {s} star  {bar}  {count}  {pts}")
    print(f"  {'═'*50}\n")


def show_leaderboard(helpers):
    ranked = sorted(helpers, key=lambda h: h["points"], reverse=True)
    print(f"\n  {'═'*60}")
    print(f"  {'LEADERBOARD':^60}")
    print(f"  {'═'*60}")
    print(f"  {'#':<4} {'Name':<22} {'Tier':<10} {'Reviews':<9} {'Avg':<7} {'Points'}")
    print(f"  {'─'*60}")
    for i, h in enumerate(ranked, 1):
        reviews = h["reviews"]
        avg = round(sum(r["stars"] for r in reviews) / len(reviews), 1) if reviews else 0
        print(f"  {i:<4} {h['name']:<22} {get_tier(h['points']):<10} "
              f"{len(reviews):<9} {avg:<7} {h['points']}")
    print(f"  {'─'*60}\n")


# ============================================================
# HELPER FACTORY
# ============================================================

def new_helper(name, speciality="General"):
    return {"name": name, "speciality": speciality, "points": 0, "reviews": []}


# ============================================================
# DEMO
# ============================================================

print("=" * 60)
print("   POINTS ADD & DEDUCT ALGORITHM — FIXED DEMO")
print("=" * 60)

sarah = new_helper("Sarah Mitchell", "Housing Support")
james = new_helper("James Okafor",   "Mental Health")
priya = new_helper("Priya Nair",     "Food and Resources")

print("\n--- Sarah gets excellent reviews ---")
apply_review(sarah, 5, "She found me emergency housing within 24 hours. Truly life-changing support.", "Maria Lopez")
apply_review(sarah, 5, "Outstanding work. Found resources I did not know existed. Above and beyond.", "Ahmed Hassan")
apply_review(sarah, 4, "Very responsive and thorough. Checked in multiple times after helping me.", "Jenny Park")
apply_review(sarah, 5, "Sarah went above and beyond for my family during a very difficult situation.", "David Chen")

print("\n--- Duplicate review attempt (should be blocked) ---")
apply_review(sarah, 1, "Trying to tank her score again!", "Maria Lopez")  # Should be blocked

print("\n--- James gets mixed reviews ---")
apply_review(james, 4, "James listened carefully and connected me to the right services quickly.", "Sarah O Brien")
apply_review(james, 3, "Helpful but took a while to respond to my first message.", "Mark Williams")
apply_review(james, 2, "Response was very slow and I had to follow up three times to get help.", "Kevin Brown")
apply_review(james, 5, "Exceptional support during a very difficult time. Truly compassionate.", "Lisa Turner")

print("\n--- Priya gets a poor review ---")
apply_review(priya, 1, "Did not show up. No communication at all.", "Emma Davis")
apply_review(priya, 3, "Eventually helped but it took much longer than expected.", "Tom Harris")
apply_review(priya, 4, "Once engaged she was professional and thorough.", "Anita Sharma")

# Summaries
print("\n--- Helper Summaries ---")
show_helper_summary(sarah)
show_helper_summary(james)
show_helper_summary(priya)

# Leaderboard
show_leaderboard([sarah, james, priya])

# ============================================================
# INTERACTIVE MODE
# ============================================================

helpers_store = {
    1: sarah,
    2: james,
    3: priya,
}

print("=" * 60)
print("  LIVE REVIEW ENTRY")
print("=" * 60)
print("  Commands:")
print("  review             → submit a review")
print("  leaderboard        → show rankings")
print("  summary <id>       → show helper detail")
print("  quit               → exit\n")

while True:
    try:
        cmd = input("  Command: ").strip().lower()

        if cmd in ("quit", "exit"):
            print("  Goodbye!\n")
            break

        elif cmd == "leaderboard":
            show_leaderboard(list(helpers_store.values()))

        elif cmd.startswith("summary "):
            try:
                hid = int(cmd.split()[1])
                if hid in helpers_store:
                    show_helper_summary(helpers_store[hid])
                else:
                    print(f"  Helper ID {hid} not found.")
            except (ValueError, IndexError):
                print("  Usage: summary <id>  e.g. summary 1")

        elif cmd == "review":
            print("\n  Available helpers:")
            for hid, h in helpers_store.items():
                print(f"    {hid}. {h['name']}  ({get_tier(h['points'])} — {h['points']} pts)")

            try:
                hid = int(input("\n  Helper ID   : ").strip())
            except ValueError:
                print("  Invalid ID — please enter a number."); continue

            if hid not in helpers_store:
                print("  Helper not found."); continue

            seeker   = input("  Your name   : ").strip() or "Anonymous"
            try:
                stars = int(input("  Stars (1-5) : ").strip())
            except ValueError:
                print("  Stars must be a number between 1 and 5."); continue

            if stars not in range(1, 6):
                print("  Stars must be 1 to 5."); continue

            feedback = input("  Feedback    : ").strip()
            apply_review(helpers_store[hid], stars, feedback, seeker)

        else:
            print("  Unknown command. Try: review / leaderboard / summary <id> / quit")

    except KeyboardInterrupt:
        print("\n  Goodbye!")
        break
    except Exception as e:
        print(f"  Error: {e}")
        continue
