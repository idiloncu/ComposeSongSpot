package com.example.composesongspot

import androidx.annotation.DrawableRes
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

sealed class Screen(val title: String, val route: String) {

    sealed class BottomScreen(
        val bTitle: String,
        val bRoute: String,
        @DrawableRes val icon: Int
    ) : Screen(bTitle, bRoute) {
        object Home : BottomScreen(
            "Home",
            "home",
            R.drawable.baseline_music_video_24
        )

        object Library : BottomScreen(
            "Find",
            "find",
            R.drawable.baseline_saved_search_24
        )

        object Message : BottomScreen(
            "Message",
            "message",
            R.drawable.baseline_chat_24
        )
    }

    sealed class CommentScreen(
        val cTitle: String,
        val cRoute: String,
    ) : Screen(cTitle, cRoute) {
        object Comment : CommentScreen(
            "Comment",
            "comment"
        )
    }

    sealed class ChatScreen(
        val hTitle: String,
        val hRoute: String,
    ) : Screen(hTitle, hRoute) {
        object ChatPage : ChatScreen(
            "Chat",
            "chat/{receiverId}"
        )

        object GroupPage : ChatScreen(
            "Group",
            "group/{groupId}"
        )

        object UserList : ChatScreen(
            "User List",
            "userlist/{groupId}"
        )
    }

    sealed class DrawerScreen(
        val dTitle: String,
        val dRoute: String,
        @DrawableRes val icon: Int
    ) : Screen(dTitle, dRoute) {
        object Account : DrawerScreen(
            "Account",
            "account",
            R.drawable.baseline_account_circle_24
        )

        object SignIn : DrawerScreen(
            "Sign In",
            "sign in",
            R.drawable.baseline_login
        )

        object Signup : DrawerScreen(
            "Sign Up",
            "sign up",
            R.drawable.baseline_person_add_alt_1_24
        )

        object SignOut : DrawerScreen(
            "Sign Out",
            "sign out",
            R.drawable.baseline_logout
        )
    }

    sealed class BottomSheet(
        fTitle: String,
        fRoute: String,
        @DrawableRes val icon: Int
    ) : Screen(fTitle, fRoute) {
        object Settings : DrawerScreen(
            "Settings",
            "settings",
            R.drawable.baseline_settings_24
        )

        object About : DrawerScreen(
            "About",
            "about",
            R.drawable.baseline_question_mark_24
        )
    }

    sealed class Comment(
        val cTitle: String,
        val cRoute: String,
    ) : Screen(cTitle, cRoute) {
        object Comment : CommentScreen(
            "Comment",
            "comment"
        )
    }

}

val screensInBottom: List<Screen.BottomScreen>
    get() {
        val currentUser = Firebase.auth.currentUser
        return if (currentUser != null) {
            listOf(
                Screen.BottomScreen.Home,
                Screen.BottomScreen.Library,
                Screen.BottomScreen.Message
            )
        } else {
            listOf(
                Screen.BottomScreen.Home,
                Screen.BottomScreen.Library
            )
        }
    }

val screensInDrawer: List<Screen.DrawerScreen>
    get() {
        val currentUser = Firebase.auth.currentUser
        return if (currentUser != null) {
            listOf(
                Screen.DrawerScreen.Account,
                Screen.DrawerScreen.SignOut
            )
        } else {
            listOf(
                Screen.DrawerScreen.Account,
                Screen.DrawerScreen.SignIn,
                Screen.DrawerScreen.Signup
            )
        }
    }

val screensInComment = listOf(
    Screen.CommentScreen.Comment
)

@Composable
fun BottomNavigationBar(
    currentScreen: Screen.BottomScreen,
    onScreenSelected: (Screen.BottomScreen) -> Unit
) {
    NavigationBar(
        containerColor = Color.Black,  // Arka plan rengini ayarlayın
        contentColor = Color.White    // İçerik rengini ayarlayın
    ) {
        Screen.BottomScreen::class.sealedSubclasses.forEach { subclass ->
            val screen = subclass.objectInstance ?: return@forEach
            NavigationBarItem(
                icon = {
                    Icon(
                        painterResource(id = screen.icon),
                        contentDescription = screen.bTitle
                    )
                },
                label = { Text(screen.bTitle) },
                selected = currentScreen == screen,
                onClick = { onScreenSelected(screen) }
            )
        }
    }
}