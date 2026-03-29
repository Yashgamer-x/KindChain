package com.unh.communityhelp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.unh.communityhelp.auth.login.view.LoginView
import com.unh.communityhelp.auth.signup.view.CompleteProfileView
import com.unh.communityhelp.auth.signup.view.SignupView
import com.unh.communityhelp.mainmenu.view.MainMenuView

enum class AppScreen {
    AuthGraph,
    Login,
    SignUp,
    CompleteProfile,

    MainMenuGraph,
    MainMenu,
}
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AppScreen.AuthGraph.name
    ) {
        authGraph(navController)
        mainMenuGraph(navController)
    }
}

fun NavGraphBuilder.authGraph(navController: NavHostController){
    navigation(
        startDestination = AppScreen.Login.name,
        route = AppScreen.AuthGraph.name
    ) {
        composable(AppScreen.Login.name) {
            LoginView(
                onNavigateToSignUp = { navController.navigate(AppScreen.SignUp.name) },
                onLoginSuccess = {
                    navController.navigate(AppScreen.MainMenuGraph.name) {
                        popUpTo(AppScreen.AuthGraph.name) { inclusive = true }
                    }
                }
            )
        }
        composable(AppScreen.SignUp.name) {
            SignupView(
                onNavigateToLogin = { navController.popBackStack() },
                onSignUpSuccess = {
                    navController.navigate(AppScreen.CompleteProfile.name)
                }
            )
        }

        composable(AppScreen.CompleteProfile.name) {
            CompleteProfileView(
                onProfileComplete = {
                    navController.navigate(AppScreen.MainMenuGraph.name) {
                        popUpTo(AppScreen.AuthGraph.name) { inclusive = true }
                    }
                }
            )
        }
    }
}

fun NavGraphBuilder.mainMenuGraph(navController: NavHostController){
    navigation(
        startDestination = AppScreen.MainMenu.name,
        route = AppScreen.MainMenuGraph.name
    ){
        composable(route = AppScreen.MainMenu.name){
            MainMenuView(onLogout = {
                navController.navigate(AppScreen.AuthGraph.name){
                    popUpTo(AppScreen.MainMenuGraph.name) { inclusive = true }
                }
            })
        }
    }
}