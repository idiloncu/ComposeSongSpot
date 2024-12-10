package com.example.composesongspot.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.composesongspot.R
import com.example.composesongspot.Screen
import com.example.composesongspot.screensInBottom
import com.example.composesongspot.screensInDrawer
import com.example.composesongspot.ui.theme.ViewModel.ChatViewModel
import com.example.composesongspot.ui.theme.ViewModel.MainViewModel
import com.example.composesongspot.ui.theme.bottom_screen.ChatScr
import com.example.composesongspot.ui.theme.bottom_screen.Comment
import com.example.composesongspot.ui.theme.bottom_screen.FindSong
import com.example.composesongspot.ui.theme.bottom_screen.GroupChatScr
import com.example.composesongspot.ui.theme.bottom_screen.Home
import com.example.composesongspot.ui.theme.bottom_screen.Message
import com.example.composesongspot.ui.theme.bottom_screen.UserList
import com.example.composesongspot.ui.theme.bottom_sheet_screen.About
import com.example.composesongspot.ui.theme.bottom_sheet_screen.Settings
import com.example.composesongspot.ui.theme.drawer_screen.Account
import com.example.composesongspot.ui.theme.drawer_screen.SignIn
import com.example.composesongspot.ui.theme.drawer_screen.SignOut
import com.example.composesongspot.ui.theme.drawer_screen.Signup
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun MainView() {
    val viewModel: MainViewModel = viewModel()
    val scaffoldState: ScaffoldState = rememberScaffoldState()
    val scope: CoroutineScope = rememberCoroutineScope()
    val controller: NavController = rememberNavController()
    val navBackStackEntry by controller.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val isSheetFullScreen by remember { mutableStateOf(false) }
    val modifier = if (isSheetFullScreen) Modifier.fillMaxSize() else Modifier.fillMaxWidth()

    val currentScreen = remember {
        viewModel.currentScreen.value
    }
    val title = remember {
        mutableStateOf(currentScreen.title)
    }
    val modalSheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        confirmValueChange = { it != ModalBottomSheetValue.HalfExpanded }
    )
    val roundedCornerRadius = if (isSheetFullScreen) 0.dp else 16.dp

    val dialogOpen = remember {
        mutableStateOf(false)
    }
    val bottomBar: @Composable () -> Unit = {
        if (currentScreen is Screen.DrawerScreen || currentScreen == Screen.BottomScreen.Home) {
            BottomNavigation(Modifier.wrapContentSize()) {
                screensInBottom.forEach { item ->
                    val isSelected = currentRoute == item.bRoute
                    val tint = if (isSelected) Color.White else Color.Black
                    BottomNavigationItem(
                        selected = currentRoute == item.bRoute,
                        onClick = {
                            controller.navigate(item.bRoute)
                            title.value = item.bTitle
                        },
                        icon = {
                            Icon(
                                painter = painterResource(id = item.icon),
                                contentDescription = item.bTitle
                            )
                        },
                        label = {
                            Text(text = item.bTitle)
                        },
                        selectedContentColor = Color.White,
                        unselectedContentColor = Color.Black
                    )
                }
            }
        }
    }
    ModalBottomSheetLayout(
        sheetState = modalSheetState,
        sheetShape = RoundedCornerShape(
            topStart = roundedCornerRadius,
            topEnd = roundedCornerRadius
        ),
        sheetContent = {
            MoreButtonSheet(navController = controller, modifier = modifier)
        }) {
        Scaffold(
            bottomBar = bottomBar,
            topBar = {
                TopAppBar(title = { Text("Song Spot") },
                    actions = {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    if (!modalSheetState.isVisible) {
                                        modalSheetState.show()
                                    } else {
                                        modalSheetState.hide()
                                    }
                                }
                            }
                        ) {
                            Icon(imageVector = Icons.Default.MoreVert, contentDescription = null)
                        }

                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch {
                                scaffoldState.drawerState.open()
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Menu"
                            )
                        }
                    }
                )
            }, scaffoldState = scaffoldState,
            drawerContent = {
                LazyColumn(Modifier.padding(16.dp)) {
                    items(screensInDrawer) { item ->
                        DrawerItem(selected = currentRoute == item.dRoute, item = item) {
                            scope.launch {
                                scaffoldState.drawerState.close()
                            }
                            if (item.dRoute == "add_account") {
                                dialogOpen.value = true

                            } else {
                                controller.navigate(item.dRoute)
                                title.value = item.dTitle
                            }
                        }
                    }
                }
            }
        ) {
            Navigation(navController = controller, viewModel = viewModel, pd = it)
        }
    }
}

@Composable
fun DrawerItem(
    selected: Boolean,
    item: Screen.DrawerScreen,
    onDrawerItemClicked: () -> Unit,
) {
    val background = if (selected) Color.Green else Color.White
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 16.dp)
            .background(background)
            .clickable {
                onDrawerItemClicked()
            }) {
        Icon(
            painter = painterResource(id = item.icon),
            contentDescription = item.dTitle,
            Modifier.padding(end = 8.dp, top = 4.dp),
            tint = Color.DarkGray
        )
        Text(
            text = item.dTitle,
            style = MaterialTheme.typography.headlineSmall,
            color = Color.DarkGray
        )
    }
}

@Composable
fun Navigation(navController: NavController, viewModel: MainViewModel, pd: PaddingValues) {
    NavHost(
        navController = navController as NavHostController,
        startDestination = Screen.BottomScreen.Home.route,
        modifier = Modifier.padding(pd)
    ) {
        composable(Screen.DrawerScreen.SignIn.route) {
            SignIn(navController, viewModel())
        }
        composable(Screen.DrawerScreen.Account.route) {
            Account()
        }
        composable(Screen.BottomScreen.Home.bRoute) {
            Home(navController)
        }
        composable(Screen.BottomScreen.Message.bRoute) {
            Message(navController)
        }
        composable(Screen.BottomScreen.Library.bRoute) {
            FindSong()
        }
        composable(Screen.BottomSheet.About.route) {
            About(navController)
        }
        composable(Screen.BottomSheet.Settings.route) {
            Settings(navController)
        }
        composable(Screen.DrawerScreen.Signup.route) {
            Signup(navController, viewModel())
        }
        composable(Screen.DrawerScreen.SignOut.route) {
            SignOut(navController, viewModel())
        }
        composable(Screen.BottomSheet.About.dRoute) {
            About(navController)
        }
        composable(Screen.BottomSheet.Settings.dRoute) {
            Settings(navController)
        }
        composable(Screen.CommentScreen.Comment.cRoute) {
            Comment(navController)
        }
        composable(Screen.ChatScreen.ChatPage.hRoute) {
            val receiverId = it.arguments?.getString("receiverId") ?: ""
            ChatScr(navController, receiverId)
        }
        composable(Screen.ChatScreen.GroupPage.hRoute) {
            val groupId = it.arguments?.getString("groupId") ?: ""
            GroupChatScr(navController, groupId)
            ChatViewModel().listenGroupChats(groupId)
        }
        composable(Screen.ChatScreen.UserList.hRoute){
            val groupId = it.arguments?.getString("groupId") ?: ""
            UserList(groupId)
        }
    }
}

@Composable
fun MoreButtonSheet(modifier: Modifier, navController: NavController) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(300.dp)
            .background(Color.White)
    )
    {
        Column(modifier = modifier.padding(16.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Row(modifier = modifier
                .padding(16.dp)
                .clickable {
                    navController.navigate(Screen.BottomSheet.Settings.dRoute)
                }

            ) {
                Icon(
                    modifier = Modifier.padding(end = 8.dp),
                    painter = painterResource(id = R.drawable.baseline_settings_24),
                    contentDescription = "Settings"
                )
                Text(
                    text = "Settings",
                    fontSize = 20.sp,
                    color = Color.DarkGray
                )
            }
            Row(modifier = modifier
                .padding(16.dp)
                .clickable {
                    navController.navigate(Screen.BottomSheet.About.dRoute)
                }
            ) {
                Icon(
                    modifier = Modifier.padding(end = 8.dp),
                    painter = painterResource(id = R.drawable.baseline_question_mark_24),
                    contentDescription = "About"
                )
                Text(
                    text = "About",
                    fontSize = 20.sp,
                    color = Color.DarkGray
                )
            }
        }
    }
}