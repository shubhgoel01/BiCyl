package com.example.rentalbicycle

import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest

import androidx.compose.material3.Divider
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.text.TextStyle
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter


@Composable
fun SignInScreen(viewModel: MainViewModel) {
    var registrationNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current
    val repository = viewModel.repository
    val auth = repository.auth
    var visible by remember{ mutableStateOf(false) }

//    var user by remember { mutableStateOf(auth.currentUser) }
//    LaunchedEffect(Unit) {
//        auth.addAuthStateListener { firebaseAuth ->
//            user = firebaseAuth.currentUser
//        }
//    }

    // Background Box for entire screen
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF3F4F6))  // Light background color
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)  // Padding for entire content
                .background(Color.White, shape = RoundedCornerShape(16.dp))  // Card-like background
                .padding(30.dp),  // Inner padding for the content
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if(visible)
                Sign_in_Guidelines{
                    visible=false
                }
            // Logo Image
            Image(
                painter = painterResource(R.drawable.bicycl),
                contentDescription = "LOGO",
                modifier = Modifier
                    .size(120.dp)  // Adjust size as needed
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.height(32.dp))  // Space between image and text fields

            // Registration Number TextField
            TextField(
                value = registrationNumber,
                onValueChange = { registrationNumber = it },
                placeholder = {
                    Text("Registration Number")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Color(0xFFF0F0F0),
                        RoundedCornerShape(8.dp)
                    )  // Rounded background for text field
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))  // Space between text fields

            // Password TextField
            TextField(
                value = password,
                onValueChange = { password = it },
                placeholder = {
                    Text("Password")
                },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Color(0xFFF0F0F0),
                        RoundedCornerShape(8.dp)
                    )  // Rounded background for text field
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))  // Space between text fields and button

            // Sign-In Button
            Button(
                onClick = {
                    if (registrationNumber.isEmpty() || password.isEmpty()) {
                        Toast.makeText(context, "Invalid", Toast.LENGTH_SHORT).show()
                    } else {
                        repository.checkStudentExist(registrationNumber, password)
                    }
                },
                colors= ButtonDefaults.buttonColors(
                    containerColor = Color.Gray,
                ),
                elevation = ButtonDefaults.buttonElevation(10.dp),
                modifier = Modifier
                    .padding(start = 20.dp, end = 20.dp)
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),  // Rounded corners for the button,
            ) {
                Text(
                    text = "Sign-In",
                    style = MaterialTheme.typography.headlineSmall,  // Updated style for Material3
                )
            }
            //Spacer(Modifier.padding(5.dp))
            Text(text = "How It Works?",modifier=Modifier.clickable {
                visible= true
            },
                color=Color.Red)
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun homeScreen(
    navController: NavController,
    viewModel: MainViewModel,
) {
    val currentDate = LocalDate.now()
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    //val formattedDate = currentDate.format(dateFormatter) this line changes localDate to string
    Log.d("Schedule date", currentDate.toString())

    var studentState by viewModel.studentState

    val scheduleState by viewModel.scheduleState.collectAsState()

    //val scheduleState by viewModel.scheduleState
    val Student = studentState.student
    val imageUrl = Student.imageURL.ifEmpty { "default" }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp)
        ) {
            Text(text = "Profile", fontWeight = FontWeight.Bold)

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(10.dp),
                tonalElevation = 4.dp,
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(modifier = Modifier.padding(10.dp)) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(if (imageUrl == "default") R.drawable.person else imageUrl)
                            .placeholder(R.drawable.person)
                            .error(R.drawable.person)
                            .build(),
                        contentDescription = "Student Image",
                        modifier = Modifier
                            .size(100.dp)
                            .padding(end = 10.dp)
                    )

                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(text = Student.name, fontSize = 20.sp, modifier = Modifier.padding(bottom = 5.dp))
                        Text(text = Student.registrationNumber, fontSize = 20.sp, modifier = Modifier.padding(bottom = 5.dp))
                        Text(text = Student.branch, fontSize = 20.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(15.dp))

            Text(text = "Schedules", fontWeight = FontWeight.Bold)

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // Adjust weight if needed
                    .padding(10.dp),
                //tonalElevation = 4.dp,
                shape = RoundedCornerShape(24.dp)
            ) {
                // Use a Column with verticalScroll
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()) // Make the Column scrollable
                        .padding(8.dp)
                ) {
                    when {
                        scheduleState.scheduleDataList.isEmpty() -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "No Schedules",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "NO SCHEDULES FOR THE DAY",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        scheduleState.error -> {
                            error(scheduleState.errorMessage)
                        }
                        scheduleState.isLoading -> {
                            LoadingPage()
                        }
                        else -> {
                            // Iterate over the list and create items manually
                            val deleteList: MutableList<Schedule> = mutableListOf() //It will contain all the schedules that have booking date less than that of current date
                            scheduleState.scheduleDataList.forEach { schedule ->
                                val BookingDate = LocalDate.parse(schedule.startDate, dateFormatter)
                                if(BookingDate.isBefore(currentDate) || schedule.endTime.toInt()<=LocalTime.now().hour)
                                {
                                    deleteList.add(schedule)
                                }
                                else{
                                    if(schedule.start_time.toInt()<LocalTime.now().hour)        //For now this variable is not used anywhere
                                            schedule.status="Ongoing"
                                    scheduleScreen(schedule,viewModel,navController)
                                }
                            }
                            if(deleteList.isNotEmpty())
                            {
                                deleteList.forEach { schedule->
                                    deleteSchedule(schedule, viewModel,navController,false){}
                                }
                                viewModel.get_schedule_list_from_schedule_List_String(viewModel.user!!.uid)
                            }
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.CenterHorizontally),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = {
                        viewModel.getAllSlots()
                        navController.navigate("availableSlotsScreen")
                    },
                    elevation = ButtonDefaults.buttonElevation(20.dp)
                ) {
                    Text(text = "Add")
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun scheduleScreen(schedule: Schedule, viewModel: MainViewModel, navController: NavController) {
    var eachScheduleDetailVisible by remember { mutableStateOf(false) }

    // Navigate to detail screen if the detail is visible
    if (eachScheduleDetailVisible) {
        navController.currentBackStackEntry?.savedStateHandle?.set("schedule", schedule)
        Log.d("scheduleScreen", "Calling function")
        navController.navigate("eachScheduleDetailScreen")
        eachScheduleDetailVisible = false // Reset visibility
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { eachScheduleDetailVisible = true },
        tonalElevation = 4.dp,
        shape = RoundedCornerShape(8.dp),
        color = if (schedule.start_time.toInt() < LocalTime.now().hour) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) // Slightly transparent for ongoing schedules
        } else {
            MaterialTheme.colorScheme.background // Default background color
        }
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = if (schedule.status == "Ongoing") "Ongoing Schedule" else "Schedule Details",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Divider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Text(text = "From ${schedule.start_time} to ${schedule.endTime}")
        }
    }
}


@Composable
fun LoadingPage() {
    // Display loading circle
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(64.dp),
            color = Color.White,  // Set the circle color to white
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeWidth = 6.dp
        )
    }
}




@Composable
fun Sign_in_Guidelines(onDismissRequest: () -> Unit) {
    Dialog(onDismissRequest = { onDismissRequest() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),  // Enable vertical scrolling
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)  // Padding inside the Card
            ) {
                Text(
                    text = "How Sign-In Works",
                    modifier = Modifier
                        .wrapContentSize()
                        .padding(bottom = 16.dp),  // Space between title and content
                    textAlign = TextAlign.Start,
                    color = Color.Red,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge  // Apply large title style
                )

                Text(
                    text = "When the user enters the registration number and password, " +
                            "first the system checks if the user is a student of NICK. " +
                            "If not, an 'ACCESS DENIED' message is displayed. " +
                            "Otherwise, the user is signed in with the registered email and the entered password.",
                    color = Color.Black,
                    modifier = Modifier
                        .fillMaxWidth(),  // Ensure the text takes up the full width
                    textAlign = TextAlign.Start,  // Align text to the start
                    style = MaterialTheme.typography.bodyMedium  // Apply body text style
                )
            }
        }
    }
}




@Composable
fun error(errorMessage:String) {
    Text(text = errorMessage)
}

@Composable
fun availableSlotsScreen(viewModel: MainViewModel,navController: NavController) {
    viewModel.Demo()
    //val slotsStatus by viewModel.slotStatus
    val _slotsStatus by viewModel.availableCycleState.collectAsState()
    val slotsStatus=_slotsStatus.allCyclesData
    Log.d("Schedule_screen slotStatus",slotsStatus.toString())
    val scrollState = rememberScrollState() // Create a ScrollState instance

    Log.d("available_slot_screen _slotStatus",_slotsStatus.toString())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState) // Apply vertical scrolling
            .padding(16.dp) // Add padding for the content
    ) {
        Text(
            text = "Available Slots",
            modifier = Modifier
                .padding(bottom = 16.dp) // Space between the title and the list
        )

        // Display each item in the list using Column
        slotsStatus.forEach { cycle ->
            cycleVisible(cycle, navController)
        }
    }
}



@Composable
fun cycleVisible(cycle: Cycle,navController: NavController) {
    // cycle is an object that contains all data of that particular cycle
    val imageURL = cycle.imageURL
    val location = cycle.location
    val id = cycle.CycleId

    for (slots in cycle.availableTime.drop(1)) {
        slotUI(imageURL, location, id, slots[0], slots[1]){
            navController.currentBackStackEntry?.savedStateHandle?.set("cycle", cycle)
            navController.currentBackStackEntry?.savedStateHandle?.set("start", slots[0])
            navController.currentBackStackEntry?.savedStateHandle?.set("end", slots[1])
            navController.navigate("slotsScreen")
        }
    }
}

@Composable
fun slotUI(imageUrl: String?, location: String, id: String, start: String, end: String,onClick:()->Unit) {
    Surface(
        modifier = Modifier
            .padding(start = 10.dp, end = 10.dp, bottom = 10.dp)
            .fillMaxWidth()
            .clickable {
                onClick()
            },
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp // Adding tonal elevation for a subtle shadow effect
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl ?: R.drawable.defaultdycle)
                    .placeholder(R.drawable.defaultdycle)
                    .error(R.drawable.defaultdycle)
                    .build(),
                contentDescription = "Cycle Image",
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "Available from $start to $end",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Location: $location",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun singleTimeSlotView(cycle: Cycle, start: String, end: String,viewModel: MainViewModel,navController: NavController) {
    val imageUrl = cycle.imageURL
    val location = cycle.location
    val id = cycle.CycleId
    var addYourTimeSlotVisible by remember{
        mutableStateOf(false)
    }

    if(addYourTimeSlotVisible)
    {
        addYourTimeSlot(
            onBook = { newstart: String, newend: String ->
                viewModel.addNewSchedule(cycle, mutableListOf(start,end), mutableListOf(newstart,newend))

                //viewModel.get_schedule_list_from_schedule_List_String(viewModel.auth.currentUser!!.uid)

                navController.navigate("Home_Screen") {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    launchSingleTop = true
                }

            },
            onDismiss = {
                addYourTimeSlotVisible = false
            }
        )
    }
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        tonalElevation = 4.dp,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp) // Adds spacing between items
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl ?: R.drawable.defaultdycle)
                    .placeholder(R.drawable.defaultdycle)
                    .error(R.drawable.defaultdycle)
                    .build(),
                contentDescription = "Cycle Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(12.dp)) // Rounds the corners of the image
            )
            Text(
                text = "Cycle ID: $id",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "Location: $location",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "Available Slot: From $start to $end",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Enter your time slot and book ride now",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = {
                          addYourTimeSlotVisible=true
                          },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = "Add")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun addYourTimeSlot(onBook:(newStart:String,newEnd:String)->Unit,onDismiss: () -> Unit,) {
    var start by remember { mutableStateOf("") }
    var end by remember { mutableStateOf("") }

    //Accessing paymentStatus from MAinActivity
    val context = LocalContext.current
    val paymentStatus = (context as MainActivity).paymentStatus // Access paymentStatus

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = {
            Text(
                text = "Select Time Slot",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "From",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        BasicTextField(
                            value = start,
                            onValueChange = { start = it },
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.background,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                .width(80.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            textStyle = TextStyle(color = Color.White)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "To",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        BasicTextField(
                            value = end,
                            onValueChange = { end = it },
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.background,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                .width(80.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            textStyle = TextStyle(color = Color.White)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        },
        confirmButton = {
            Button(

                onClick = {
                    initPayment(context)
//                    onBook(start,end)
                    },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Book")
            }
            Text(text = "To Cancel - Touch anywhere on screen.",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth())
        }
    )

    //This will be called once payment is done and then cycle will be booked
    paymentStatus.value?.let { isSuccess ->
        if (isSuccess) {
            Log.d("Payment","Calling onBook function")
            onBook(start,end)

            paymentStatus.value=null
            // Call your booking function here if needed
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun eachScheduleDetailScreen(schedule: Schedule, viewModel: MainViewModel, navController: NavController) {
    Log.d("eachScheduleDetailScreen", "Calling function")
    val cycle by viewModel.particularCycleData.collectAsState()

    var deleteScheduleStatus by remember {
        mutableStateOf(false)
    }

    viewModel.fetchParticularCycleDetails(schedule.cycleUId)

    if (cycle == null) {
        error(errorMessage = "Error Occurred")
        return
    }

    val imageUrl = cycle!!.imageURL
    val location = cycle!!.location
    val id = cycle!!.CycleId

    if (deleteScheduleStatus) {
        deleteSchedule(schedule, viewModel, navController) {
            deleteScheduleStatus=false
        }
    }


    Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        tonalElevation = 4.dp,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp) // Adds spacing between items
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl ?: R.drawable.defaultdycle)
                    .placeholder(R.drawable.defaultdycle)
                    .error(R.drawable.defaultdycle)
                    .build(),
                contentDescription = "Cycle Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(12.dp)) // Rounds the corners of the image
            )
            Text(
                text = "Cycle ID: $id",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "Location: $location",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "Slot: From ${schedule.start_time} to ${schedule.endTime}",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = if(schedule.start_time.toInt() < LocalTime.now().hour)"Status: Ongoing" else "Status: Booked" ,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text ="Total Fare: " ,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            if(schedule.start_time.toInt() > LocalTime.now().hour)
            {
                Text(
                    text =if(schedule.start_time.toInt() > LocalTime.now().hour) "Cancelling a Schedule incurs a penalty" else "No Refund will be provided.",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Button(
                onClick = {
                    deleteScheduleStatus = true
                    //TODO
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = if(schedule.start_time.toInt() > LocalTime.now().hour) "Delete Schedule" else "Return Cycle")
            }
        }
    }
}

    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    fun deleteSchedule(
        schedule: Schedule,
        viewModel: MainViewModel,
        navController: NavController,
        flag: Boolean =true,  //You wants to update the ui or not
        onDismiss: () -> Unit
    ) {
        var deleteScheduleStatus by remember {
            mutableStateOf(false)
        }

        if (deleteScheduleStatus || !flag) {
            LaunchedEffect(Unit) {
                deleteScheduleStatus = false
                viewModel.deleteSchedule(schedule,flag)
                navController.navigate("Home_Screen") {
                    popUpTo("Home_Screen") { inclusive = true } //clear the back stack
                }
                onDismiss()
            }
        }
        if(!flag)
                return;
        AlertDialog(
            onDismissRequest = { onDismiss() },
            title = {
                Text(
                    text = "Delete Slot",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp), // Add spacing between text elements
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = if(schedule.start_time.toInt() > LocalTime.now().hour) "Are you sure you want to delete this schedule?" else "Are You Sure You Want to return the cycle",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    if(schedule.start_time.toInt() > LocalTime.now().hour)
                    {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Penalty: ",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "₹100", // Example penalty value, replace with your logic
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error // Highlight penalty in red
                            )
                        }
                    }

                }
            },
            confirmButton = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp), // Spacing between button and cancel message
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Button(
                        onClick = {
                            deleteScheduleStatus=true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error // Red button for delete action
                        )
                    ) {
                        Text(text =if(schedule.start_time.toInt() > LocalTime.now().hour) "Delete" else "Return", color = Color.White)
                    }
                    Text(
                        text = "Tap anywhere on the screen to cancel.",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            modifier = Modifier.padding(8.dp) // Add padding around the alert dialog
        )
    }


@Composable
fun adminLogin(viewModel: MainViewModel) {
    var idnumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current
    val repository = viewModel.repository


    // Background Box for entire screen
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF3F4F6))  // Light background color
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)  // Padding for entire content
                .background(Color.White, shape = RoundedCornerShape(16.dp))  // Card-like background
                .padding(30.dp),  // Inner padding for the content
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "Admin Login",color=Color.Red, fontSize = 20.sp,fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(32.dp))
            // Logo Image

            Spacer(modifier = Modifier.height(32.dp))  // Space between image and text fields

            // Registration Number TextField
            TextField(
                value = idnumber,
                onValueChange = { idnumber = it },
                placeholder = {
                    Text("Admin ID")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Color(0xFFF0F0F0),
                        RoundedCornerShape(8.dp)
                    )  // Rounded background for text field
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))  // Space between text fields

            // Password TextField
            TextField(
                value = password,
                onValueChange = { password = it },
                placeholder = {
                    Text("Password")
                },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Color(0xFFF0F0F0),
                        RoundedCornerShape(8.dp)
                    )  // Rounded background for text field
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))  // Space between text fields and button

            // Sign-In Button
            Button(
                onClick = {
                    if (idnumber.isEmpty() || password.isEmpty()) {
                        Toast.makeText(context, "Invalid", Toast.LENGTH_SHORT).show()
                    } else {
                        // TODO
                    }
                },
                colors= ButtonDefaults.buttonColors(
                    containerColor = Color.Gray,
                ),
                elevation = ButtonDefaults.buttonElevation(10.dp),
                modifier = Modifier
                    .padding(start = 20.dp, end = 20.dp)
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),  // Rounded corners for the button,
            ) {
                Text(
                    text = "Sign-In",
                    style = MaterialTheme.typography.headlineSmall,  // Updated style for Material3
                )
            }
        }
    }
}


