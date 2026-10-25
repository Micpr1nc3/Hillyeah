package com.example.ui.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Trails : Screen("trails")
    data class TrailDetail(val trailId: String) : Screen("trail_detail/$trailId")
    data object Hikes : Screen("hikes")
    data class HikeDetail(val hikeId: String) : Screen("hike_detail/$hikeId")
    data class HostHike(val prefillTrailId: String? = null) : Screen("host_hike")
    data object Dashboard : Screen("dashboard")
    data object Community : Screen("community")
    data object Profile : Screen("profile")
    data object Welcome : Screen("welcome")
    data class Login(val returnToScreen: Screen? = null) : Screen("login")
    data class Register(val returnToScreen: Screen? = null) : Screen("register")
    data object ProfileSetup : Screen("profile_setup")
    data class MemberProfile(val userId: String) : Screen("member_profile/$userId")
    data class InviteResponse(val hikeId: String, val senderId: String? = null) : Screen("invite/$hikeId")
}
