package com.example.rentalbicycle

import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

//---------------------------------Experiment----------------------------------------------------------------------------------------

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun myapp() {
    val scaffoldState= rememberScaffoldState()
    val scope= rememberCoroutineScope()

    val viewModel: MainViewModel = viewModel()
    val repository = viewModel.repository
    val auth = repository.auth
    val navController = rememberNavController()

    // Remember the user state
    var user by remember { mutableStateOf(auth.currentUser) }
    val studentState by viewModel::studentState

    // Handle auth state changes

    //mutableStateOf does not automatically react to changes in the auth.currentUser unless you explicitly
    // update it. The Firebase Auth state listener you added does this

    //Firebase's authentication state doesn't directly inform the Compose state when it changes. To bridge this gap,
    // you use the AuthStateListener to detect when the authentication state changes and then update your mutableStateOf variable
    LaunchedEffect(Unit) {
        val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            user = firebaseAuth.currentUser
            if (user != null) {
                Toast.makeText(auth.app.applicationContext, "You Are Signed In", Toast.LENGTH_LONG).show()
            }
        }
        auth.addAuthStateListener(authStateListener)
    }
    Log.d("User", auth.currentUser.toString())

    // Define start destination based on user authentication status
    val startDestination: String = if (user != null) {
        "Home_Screen"
    } else {
        "Login_Screen"
    }


    Scaffold(
        topBar = {ViewTopAppBar("Bicyl",scope,scaffoldState,auth)},
        drawerContent = {
            drawerContent(scope,scaffoldState,navController,repository.admin)
        },
        scaffoldState=scaffoldState,
        backgroundColor = Color.Transparent
    )
    {
        NavHost(navController = navController, startDestination = startDestination, modifier = Modifier.padding(it)) {
            composable("Login_Screen") {
                SignInScreen(viewModel)
            }
            composable("Register_Screen") {
                // Register_Page_UI(auth = auth, database = database, navController)
            }
            composable("Home_Screen") {
                when {
                    studentState.value.error -> {
                        navController.previousBackStackEntry?.savedStateHandle?.set("errorKey", studentState.value.errorMessage)
                        navController.navigate("Error_Screen")
                    }
                    studentState.value.student != Student() -> homeScreen(
                        navController,
                        viewModel
                    )
                    else -> LoadingPage()
                }
            }
            composable("availableSlotsScreen") {
                // Register_Page_UI(auth = auth, database = database, navController)
                availableSlotsScreen(viewModel,navController)
            }
            composable("slotsScreen") {
                // Register_Page_UI(auth = auth, database = database, navController)
                val cycle = navController.previousBackStackEntry?.savedStateHandle?.get<Cycle>("cycle")
                val start=navController.previousBackStackEntry?.savedStateHandle?.get<String>("start")
                val end = navController.previousBackStackEntry?.savedStateHandle?.get<String>("end")
                if(cycle!=null && start!=null && end!=null)
                    singleTimeSlotView(cycle,start,end,viewModel,navController)
                else
                {
                    Log.e("Error","Error While Navigating to slotscreen in dereferencing data from savedstatehandle")
                    error(errorMessage = "Error While Navigating to slotscreen in dereferencing data from savedstatehandle")
                }

            }
            composable("eachScheduleDetailScreen"){
                val schedule = navController.previousBackStackEntry?.savedStateHandle?.get<Schedule>("schedule")
                if(schedule==null) {
                    Log.e("Navigation","schedule is null")
                    error("Error Occurred")
                }
                else {
                    Log.e("Navigation","schedule fetched successfully")
                    eachScheduleDetailScreen(schedule, viewModel, navController)
                }
            }
            composable("LogOut") {
                LogOut(auth = auth)
            }
            composable("adminLogin"){
                adminLogin(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewTopAppBar(title:String, scope: CoroutineScope, scaffoldState: ScaffoldState,auth: FirebaseAuth) {
    val context= LocalContext.current
    TopAppBar(
        title ={
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(color = Color.White), // Customize style
                    textAlign = TextAlign.Center,
                    modifier=Modifier.padding(top=15.dp,start=105.dp)
                )
            },
        colors= TopAppBarDefaults.topAppBarColors(
            titleContentColor = Color.White,
            navigationIconContentColor = Color.White,
            containerColor = Color.Transparent,
            scrolledContainerColor = Color.Gray
        ),
        navigationIcon = {
            IconButton(onClick = {
                if(auth.currentUser==null)
                {
                    Toast.makeText(context,"Please Login",Toast.LENGTH_SHORT).show()
                    return@IconButton
                }
                scope.launch {
                    scaffoldState.drawerState.open()
                }
            }) {
                Icon(imageVector = Icons.Default.Menu, contentDescription = null,modifier=Modifier.padding(top=7.dp))
            }
        },
        modifier= Modifier
            .fillMaxWidth()
            .padding(start = 10.dp)
            .height(50.dp)
    )
}

@Composable
fun drawerContent(scope: CoroutineScope, scaffoldState: ScaffoldState,navController:NavController,admin:Boolean) {
    val context= LocalContext.current
    var menuList by remember { mutableStateOf(listOf<MenuItem>()) }


    Box(modifier=Modifier.fillMaxSize())
    {
        Column {
            Spacer(modifier = Modifier.padding(20.dp))
            Text(text = "MENU", modifier = Modifier
                .padding(start = 20.dp)
                .clickable {
                    scope.launch {
                        scaffoldState.drawerState.close()
                    }
                }, color = Color.Red
            )

            if (!admin) {
                menuList = listOf(
                    MenuItem(title = "Home", icon = Icons.Filled.Home, isOpen = true),
                    MenuItem(title = "History", icon = Icons.Filled.CheckCircle, isOpen = false),
                    MenuItem(title = "Premium", icon = Icons.Filled.Star, isOpen = false),
                    MenuItem(title = "Admin Login", icon = Icons.Filled.AddCircle, isOpen = false),
                    MenuItem(title = "Log Out", icon = Icons.Filled.AccountCircle, isOpen = false)
                )
            } else {
                menuList = listOf(
                    MenuItem(title = "Student DashBoard", icon = Icons.Filled.AccountCircle, isOpen = false),
                    MenuItem(title = "Cycle DashBoard", icon = Icons.Filled.AccountCircle, isOpen = false),
                    MenuItem(title = "Schedules DashBoard", icon = Icons.Filled.CheckCircle, isOpen = false),
                    MenuItem(title = "History", icon = Icons.Filled.CheckCircle, isOpen = false),
                    MenuItem(title = "Log Out", icon = Icons.Filled.AccountCircle, isOpen = false)
                )
            }

            menuList.forEach { item ->
                ViewContent(item = item) { clickedItem ->//this is parameter
//                menuList = menuList.map { currentItem ->                                      Do Not Delete IMPORTANT FOR FUTURE
//                    currentItem.copy(isOpen = currentItem == clickedItem)
//                }

                    when (clickedItem.title) {
                        "Home" -> navController.navigate("Home_Screen")
                        "Premium" -> {  //Like user can cancel rides up to some limit without paying the fine
                            Toast.makeText(context, "Coming Soon", Toast.LENGTH_SHORT).show()
                        }

                        "Admin Login" -> {
                            navController.navigate("adminLogin")
                        }

                        "Log Out" -> navController.navigate("LogOut")
                    }
                    //close the drawer
                    scope.launch {
                        scaffoldState.drawerState.close()
                    }
                }
            }
        }
            //Spacer(modifier = Modifier.weight(1f)) // Takes all the available space above the watermark

            // Watermark text at the bottom right
        Column(modifier = Modifier  // Adjust size as needed
            .align(Alignment.BottomEnd)
            .wrapContentSize()
            .padding(bottom=10.dp,end=10.dp)
        )
        {
            Image(
                painter = painterResource(R.drawable.waterark),
                contentDescription = "LOGO",
                modifier = Modifier
                    .size(50.dp)  // Adjust size as needed
                //.align(Alignment.BottomEnd)
            )
            Text(
                text = "@realBeast",
                modifier = Modifier,
                    //.align(Alignment.BottomEnd), // Aligns the text to the bottom end
                color = Color.Gray // You can customize the color
            )
        }

    }
}


@Composable
fun ViewContent(item:MenuItem , onClick:(item:MenuItem)->Unit) {
    val title=item.title
    val icon=item.icon
    //val status by remember{ mutableStateOf(item.isOpen) }
    Surface(
        modifier= Modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(start = 20.dp, top = 20.dp, end = 20.dp)
            .border(
                width = 2.dp,
                color = if (item.isOpen) Color.Gray else Color.White,
                shape = RoundedCornerShape(20.dp)
            ) ,
        //color= if (status) Color.Gray else Color.White,
        shape = RoundedCornerShape(20.dp)
    )
    {
        Row(modifier= Modifier.fillMaxSize(),verticalAlignment= Alignment.CenterVertically)
        {
            Icon(imageVector = icon, contentDescription =null, Modifier.padding(start=if (!item.isOpen) 20.dp else 50.dp))
            Text(text = title,
                modifier = Modifier
                    .padding(start = 5.dp)
                    .clickable {
                        if (item.isOpen)
                            return@clickable
                        onClick(item)
                        //then navigate to screen
                    },

                fontSize = 20.sp,
            )
        }
    }
}

@Composable
fun LogOut(auth:FirebaseAuth) {
    if(auth.currentUser!=null){
        auth.signOut()
        Toast.makeText(auth.app.applicationContext,"Log Out Successful",Toast.LENGTH_SHORT).show()
    }
    else
        Toast.makeText(auth.app.applicationContext,"Already Log Out",Toast.LENGTH_SHORT).show()
}



//---------------------------------------Without Nav-Bar---------------------------------------------------
//@Composable
//fun myapp() {
//    val viewModel: MainViewModel = viewModel()
//    val repository = viewModel.repository
//    val auth = repository.auth
//    val navController = rememberNavController()
//
//    // Remember the user state
//    var user by remember { mutableStateOf(auth.currentUser) }
//    val studentState by viewModel::studentState
//
//    // Handle auth state changes
//    LaunchedEffect(Unit) {
//        val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
//            user = firebaseAuth.currentUser
//            if (user != null) {
//                Toast.makeText(auth.app.applicationContext, "You Are Signed In", Toast.LENGTH_LONG).show()
//            }
//        }
//        auth.addAuthStateListener(authStateListener)
//    }
//
//    Log.d("User", auth.currentUser.toString())
//
//    // Define start destination based on user authentication status
//    val startDestination: String = if (user != null) {
//        "Home_Screen"
//    } else {
//        "Login_Screen"
//    }
//
//    // Navigation setup
//    NavHost(navController = navController, startDestination = startDestination) {
//        composable("Login_Screen") {
//            SignInScreen(viewModel)
//        }
//        composable("Register_Screen") {
//            // Register_Page_UI(auth = auth, database = database, navController)
//        }
//        composable("Home_Screen") {
//            when {
//                studentState.value.error -> {
//                    navController.previousBackStackEntry?.savedStateHandle?.set("errorKey", studentState.value.errorMessage)
//                    navController.navigate("Error_Screen")
//                }
//                studentState.value.student != Student() -> homeScreen(
//                    navController,
//                    viewModel
//                )
//                else -> LoadingPage()
//            }
//        }
//        composable("availableSlotsScreen") {
//            // Register_Page_UI(auth = auth, database = database, navController)
//            availableSlotsScreen(viewModel,navController)
//        }
//        composable("slotsScreen") {
//            // Register_Page_UI(auth = auth, database = database, navController)
//            val cycle = navController.previousBackStackEntry?.savedStateHandle?.get<Cycle>("cycle")
//            val start=navController.previousBackStackEntry?.savedStateHandle?.get<String>("start")
//            val end = navController.previousBackStackEntry?.savedStateHandle?.get<String>("end")
//            if(cycle!=null && start!=null && end!=null)
//                singleTimeSlotView(cycle,start,end,viewModel,navController)
//            else
//            {
//                Log.e("Error","Error While Navigating to slotscreen in dereferencing data from savedstatehandle")
//                error(errorMessage = "Error While Navigating to slotscreen in dereferencing data from savedstatehandle")
//            }
//
//        }
//        composable("eachScheduleDetailScreen"){
//            val schedule = navController.previousBackStackEntry?.savedStateHandle?.get<Schedule>("schedule")
//            if(schedule==null) {
//                Log.e("Navigation","schedule is null")
//                error("Error Occurred")
//            }
//            else {
//                Log.e("Navigation","schedule fetched successfully")
//                eachScheduleDetailScreen(schedule, viewModel, navController)
//            }
//        }
//    }
//}