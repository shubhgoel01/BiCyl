package com.example.rentalbicycle

import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.android.play.integrity.internal.i
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter

//in user, i am only storing the list of id of nodes of schedules. Whenever
//user creates a new schedule, a Schedule object is created containing all details,
//and and the Schedule object id is passed to user and cycle.



class Repository {
    var auth: FirebaseAuth = Firebase.auth // Firebase Authentication
    var database = Firebase.database.reference // Firebase Database reference

    var studentState = mutableStateOf(studentDataState())      //contains data of current student
    var currentUser: MutableState<user> = mutableStateOf(user())            //contains data of current user
    var scheduleListString: MutableList<String> by mutableStateOf(mutableListOf())

    var admin:Boolean=currentUser.value.isAdmin

    // MutableStateFlow holds the current state of schedulesDataState
    private var _scheduleState = MutableStateFlow(schedulesDataState())

    // Expose as read-only StateFlow
    val scheduleState: StateFlow<schedulesDataState> = _scheduleState

    //var scheduleState = mutableStateOf((schedulesDataState()))     //Do not recompose correctly
    //var availabeCycleState = mutableStateOf(getAvailableSlotsStatus())     //Contains data of all available cycles

    private val _availableCycleState = MutableStateFlow(getAvailableSlotsStatus())

    // Expose StateFlow as read-only
    val availableCycleState: StateFlow<getAvailableSlotsStatus> = _availableCycleState

    fun checkStudentExist(registrationNumber: String, password: String) {
        Log.d("Login", "Entered Registration Number $registrationNumber")
        val refStudents = database.child("students").child(registrationNumber)

        refStudents.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val student = snapshot.getValue(Student::class.java)
                if (student != null) {
                    Toast.makeText(
                        auth.app.applicationContext,
                        "Student Validated Successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    CoroutineScope(Dispatchers.Main).launch {
                        signInUser(registrationNumber, password)
                    }
                }
            } else {
                studentState.value = studentState.value.copy(
                    isLoading = false,
                    error = true,
                    errorMessage = "User Not Allowed"
                )
                Toast.makeText(
                    auth.app.applicationContext,
                    "User Not Allowed",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }.addOnFailureListener { exception ->
            studentState.value = studentState.value.copy(
                isLoading = false,
                error = true,
                errorMessage = "Error occurred while checking for the user: ${exception.message}"
            )
            Log.e("Student Check", "Error occurred while checking for the user", exception)
        }
    }

    suspend fun get_Student_From_Uid(uid: String) = withContext(Dispatchers.IO) {
        Log.d("Student Data", "UID $uid")
        try {
            val snapshot = database.child("roll_to_uid_map").child(uid).get().await()
            Log.d("Student Data", "Snapshot data $snapshot")
            val registrationNumber = snapshot.getValue(String::class.java)

            Log.d("Student Data", "Registration Number ${registrationNumber.toString()}")
            if (!registrationNumber.isNullOrEmpty()) {
                getStudentData(registrationNumber)

            } else
                studentState.value = studentState.value.copy(
                    isLoading = false,
                    error = false,
                    errorMessage = "registrationNumber is null or empty"
                )

        } catch (exception: Exception) {
            Log.e("DataRead", "Failed to read data", exception)
            studentState.value = studentState.value.copy(
                isLoading = false,
                error = true,
                errorMessage = "Failed to read registration number from Firebase UID $exception"
            )
        }
    }

    private suspend fun getStudentData(registrationNumber: String) = withContext(Dispatchers.IO) { //sets studentState
        Log.d("Student Data", "Registration Number $registrationNumber")
        try {
            val refStudents =
                Firebase.database.reference.child("students").child(registrationNumber)
            val snapshot = refStudents.get().await()
            if (snapshot.exists()) {
                val student = snapshot.getValue(Student::class.java)
                studentState.value = if (student != null) {
                    studentState.value.copy(
                        isLoading = false,
                        error = false,
                        student = student
                    )
                } else {
                    studentState.value.copy(
                        error = true,
                        errorMessage = "Error while fetching data while de-referencing"
                    )
                }
            }
        } catch (exception: Exception) {
            Log.e("DataRead", "Failed to read data", exception)
            studentState.value = studentState.value.copy(
                isLoading = false,
                error = true,
                errorMessage = "Failed to read student data"
            )
        }
    }

    private suspend fun createNewUser(student: Student, password: String) =
        withContext(Dispatchers.IO) {
            Log.e("Register", "Student email - ${student.email}")
            try {
                val authResult =
                    auth.createUserWithEmailAndPassword(student.email, password).await()
                if (authResult.user != null) {
                    Log.e("Register", "Register Successful")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            auth.app.applicationContext,
                            "User Created Successfully.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    addToDatabase(student)
                }
            } catch (e: Exception) {
                Log.e("Register", "Register error - ${e.message}")
                studentState.value = studentState.value.copy(
                    isLoading = false,
                    error = true,
                    errorMessage = "Authentication Failed: ${e.message}"
                )
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        auth.app.applicationContext,
                        "Authentication Failed: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

    private suspend fun addToDatabase(student: Student) = withContext(Dispatchers.IO) {
        auth.currentUser?.let { user ->
            val userRef = database.child("Users").child(user.uid)
            val rollMapRef = database.child("roll_to_uid_map").child(user.uid)
            try {
                val default_list = mutableListOf("Default")
                userRef.setValue(user(student.registrationNumber, default_list)).await()
                rollMapRef.setValue(student.registrationNumber).await()
                Log.e("Register", "User and Roll-to-UID map added successfully")
            } catch (e: Exception) {
                Log.e("Register", "Failed to add user data: ${e.message}")
                studentState.value = studentState.value.copy(
                    isLoading = true,
                    error = true,
                    errorMessage = "Failed to add user data"
                )
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun add_new_schedule(
        cycle: Cycle,
        prevTimeSlot: MutableList<String>,
        newTimeSlot: MutableList<String>
    ) {
        val database= Firebase.database.reference
        val cycleUid = database.child("cycleIdToUId").child(cycle.CycleId).get().await().getValue(String::class.java)

        // Get current date
        val currentDate = LocalDate.now()
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val formattedDate = currentDate.format(dateFormatter)   // This line converts currentDate to string and store/display it in a particular format
        Log.d("Schedule date", formattedDate)

        // Create a new Schedule object
        val newSchedule = Schedule(
            userUId = auth.currentUser!!.uid,
            cycleUId = cycleUid!!,
            start_time = newTimeSlot[0],
            endTime = newTimeSlot[1],
            scheduleUID = "", // Will be updated later
            startDate = formattedDate,  // Store as String
            status = "Booked"
        )

        val scheduleRef = database.child("Schedule")
        val cycleRef = database.child("Cycle").child(cycleUid)
        val newScheduleUID = scheduleRef.push().key ?: throw Exception("Failed to generate Schedule UID")

        // Set the new schedule UID
        newSchedule.scheduleUID = newScheduleUID

        // Store the schedule in the database
        scheduleRef.child(newScheduleUID).setValue(newSchedule).await()

        val updatedCycleList = cycle.scheduleList.toMutableList().apply {
            add(newScheduleUID)
        }
        val updatedUserScheduleList = currentUser.value.scheduleList.toMutableList().apply {
            add(newScheduleUID)
        }

        val updatedAvailableTimeList = cycle.availableTime.toMutableList()
        updatedAvailableTimeList.remove(prevTimeSlot)

        if (prevTimeSlot[0].toInt() < newTimeSlot[0].toInt()) {
            val remainingStartTime = prevTimeSlot[0]
            val remainingEndTime = newTimeSlot[0]
            updatedAvailableTimeList.add(mutableListOf(remainingStartTime, remainingEndTime))
        }
        if (prevTimeSlot[1].toInt() > newTimeSlot[1].toInt()) {
            val remainingStartTime = newTimeSlot[1]
            val remainingEndTime = prevTimeSlot[1]
            updatedAvailableTimeList.add(mutableListOf(remainingStartTime, remainingEndTime))
        }

        // Update cycle and user data in the database
        cycleRef.child("availableTime").setValue(updatedAvailableTimeList).await()
        cycleRef.child("scheduleList").setValue(updatedCycleList).await()
        database.child("Users").child(auth.currentUser!!.uid).child("scheduleList").setValue(updatedUserScheduleList).await()

        Log.d("Add Schedule Update", "Update completed")

        get_schedule_list_from_schedule_List_String(auth.currentUser!!.uid)
    }



    private suspend fun signInUser(registrationNumber: String, password: String) =
        withContext(Dispatchers.IO) {
            getStudentData(registrationNumber)
            try {
                studentState.value.student.email.let {
                    auth.signInWithEmailAndPassword(it, password).await()
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            auth.app.applicationContext,
                            "SignIn Successful.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                Log.d("User", "New User: creating New database instance")
                createNewUser(studentState.value.student, password)
            }
        }


    private suspend fun get_schedule_list_string_from_uid(uid: String) {    //This can be updated
        try {
            database.keepSynced(true)   //to ensure fresh data is fetched instead of local cache
            val userRef = database.child("Users").child(uid).get().await()
            var user = userRef.getValue(user::class.java)
            if (user != null) {
                currentUser.value = user
                scheduleListString = user.scheduleList // Ensure it's not null
                // Log the scheduleListString to verify its content
                Log.d("Schedule_fetch", scheduleListString.toString())
            }
            else Log.d("Schedule_fetch", "User_is_null")
        } catch (e: Exception) {
            Log.d("Student Data", "Error Occurred while retrieving schedule_list_string_from_uid $e")
        }
    }

    suspend fun get_schedule_list_from_schedule_List_String(uid: String) =
        withContext(Dispatchers.IO) {
            try {

                var temp: MutableList<Schedule> = mutableListOf()

                get_schedule_list_string_from_uid(uid)
                val snapshot = database.child("Schedule")
                Log.d("Schedule_Not_Loaded",scheduleListString.toString())
                for (it in scheduleListString.drop(1)) {  // drop(1) skips the first element
                    val schedule = snapshot.child(it).get().await().getValue(Schedule::class.java)
                    if (schedule != null) {
                        //Error In updating this
                        temp.add(schedule)
                        //_scheduleState.value.scheduleDataList.add(schedule)
                    }
                }
                _scheduleState.value = _scheduleState.value.copy(
                    scheduleDataList=temp,
                    isLoading = false
                )
            } catch (exception: Exception) {
                _scheduleState.value = _scheduleState.value.copy(
                    isLoading = false,
                    error = true,
                    errorMessage = "Error Occurred while fetching schedule"
                )
            }
        }


suspend fun getCycles()
{
    var allCyclesData: MutableList<Cycle> = mutableListOf()
    Log.d("Schedule_fetch", scheduleListString.toString())
    try
    {
        val cycleref = database.child("Cycle").get().await()
        var cycleList: List<Cycle>? = null
        Log.d("GetCycle","Inside Function")
        if (cycleref.exists()) {
            cycleList = cycleref.children.mapNotNull { it.getValue(Cycle::class.java) }
            Log.d("GetCycle","Successfully fetched data")
        }
        else{
            _availableCycleState.value=_availableCycleState.value.copy(
                isLoading = false,
                error = true,
                errorMessage = "Cycle Reference does not exist"
            )
            Log.d("GetCycle","Error while fetching data")
        }
        Log.d("GetCycle","Cycle list ${cycleList.toString()}")
        if (cycleList != null) {
            for (cycles in cycleList.drop(1) ) {
                allCyclesData.add(cycles)
                Log.d("GetCycle",cycles.toString())
            }
            _availableCycleState.value=_availableCycleState.value.copy(
                allCyclesData=allCyclesData
            )
        }else{
            _availableCycleState.value=_availableCycleState.value.copy(
                isLoading = false,
                error = true,
                errorMessage = "Cyle List is empty: NO SLOTS AVAILABLE"
            )
            Log.d("GetCycle","Error while fetching data")
        }
    }
    catch (e:Exception){
        _availableCycleState.value=_availableCycleState.value.copy(
            isLoading = false,
            error = true,
            errorMessage = "Error Occurred While Fetching Cycles $e"
        )
    }
}


    suspend fun fetchParticularCycleDetails(cycleUid:String):Cycle?
    {
        Log.d("Repository","Function called:Fetching data now")
        var cycle: Cycle? = null
        try{
            val cycleref = database.child("Cycle").child(cycleUid).get().await()
            if (cycleref.exists()) {
                cycle = cycleref.getValue(Cycle::class.java)
            }else{
                Log.e("fetchParticularCycleDetails ","cycleUID does not exist")
            }
        }
        catch (e:Exception){
            Log.e("fetchParticularCycleDetails ","Error While Fetch Data")
        }
        return cycle
    }

    suspend fun deleteSchedule(schedule: Schedule,flag:Boolean=true) {
        val userRef = database.child("Users").child(schedule.userUId)
        val cycleRef = database.child("Cycle").child(schedule.cycleUId)

        // Update the user schedule list
        val updatedScheduleList: MutableList<String> = mutableListOf()
        for (x in scheduleListString) {
            if (x != schedule.scheduleUID) {
                updatedScheduleList.add(x)
            }
        }

        try {
            userRef.child("scheduleList").setValue(updatedScheduleList).await()
            Log.d("Firebase", "User schedule list updated successfully")
        } catch (e: Exception) {
            Log.e("Firebase", "Error updating user schedule list", e)
        }

        // Update the cycle schedule list
        val updatedCycleList = mutableListOf<String>()
        try {
            val snapshot = cycleRef.child("scheduleList").get().await()
            val cycleScheduleList = snapshot.children.mapNotNull { it.getValue(String::class.java) }

            for (x in cycleScheduleList) {
                if (x != schedule.scheduleUID) {
                    updatedCycleList.add(x)
                }
            }

            if (updatedCycleList.isEmpty()) {
                updatedCycleList.add("Default") // Ensure at least one entry if needed
            }

            cycleRef.child("scheduleList").setValue(updatedCycleList).await()
            Log.d("Firebase", "Cycle schedule list updated successfully")
        } catch (e: Exception) {
            Log.e("Firebase", "Error updating cycle schedule list", e)
        }

        // Delete the schedule node
        try {
            val deleteScheduleRef = database.child("Schedule").child(schedule.scheduleUID)
            deleteScheduleRef.removeValue().await()
            Log.d("Firebase", "Schedule successfully deleted.")
        } catch (e: Exception) {
            Log.e("Firebase", "Error deleting schedule: $e")
        }

        if(flag)
            get_schedule_list_from_schedule_List_String(auth.currentUser!!.uid)     //TODO this can be optimized

        var cycleAvailableTimeList: MutableList<MutableList<String>> = mutableListOf()
        try {
            val snapshot = cycleRef.child("availableTime").get().await()
            cycleAvailableTimeList = snapshot.children.mapNotNull { childSnapshot ->
                childSnapshot.children.mapNotNull { timeRangeSnapshot ->
                    timeRangeSnapshot.getValue(String::class.java)
                }.toMutableList()
            }.toMutableList()
        } catch (e: Exception) {
            Log.e("Firebase", "Error fetching available time list", e)
        }

        val updatedCycleAvailableTime = addAndMergeTimeRange(cycleAvailableTimeList, listOf(schedule.start_time, schedule.endTime)).toMutableList()

        try {
            cycleRef.child("availableTime").setValue(updatedCycleAvailableTime).await()
            Log.d("Delete Schedule", "Successfully updated cycle available time")
        } catch (e: Exception) {
            Log.e("Delete Schedule", "Error updating cycle available time: $e")
        }
    }

    fun addAndMergeTimeRange(
        existingTimes: MutableList<MutableList<String>>,
        newRange: List<String>
    ): List<List<String>> { // Change return type to List<List<String>>
        // Convert each time range to a pair of start and end integers for easier comparison
        val timesAsPairs = existingTimes.drop(1) // Skip header row
            .map { it[0].toInt() to it[1].toInt() } // Assuming each list has exactly 2 elements

        // Convert new range into a pair
        val newStart = newRange[0].toInt()
        val newEnd = newRange[1].toInt()

        // Add the new range to the list
        val updatedTimes = timesAsPairs.toMutableList()
        updatedTimes.add(newStart to newEnd)

        // Sort by start time
        updatedTimes.sortBy { it.first }

        // Merge overlapping ranges
        val mergedTimes = mutableListOf<Pair<Int, Int>>()
        var current = updatedTimes[0]

        for (i in 1 until updatedTimes.size) {
            val next = updatedTimes[i]
            if (current.second >= next.first) {
                // If ranges overlap, merge them
                current = current.first to maxOf(current.second, next.second)
            } else {
                // Add the current range to the list and move to the next
                mergedTimes.add(current)
                current = next
            }
        }
        mergedTimes.add(current) // Add the last range

        // Build and return the updated availableTime list as a List<List<String>>
        return listOf(mutableListOf("Schedule")) +
                mergedTimes.map { listOf(it.first.toString(), it.second.toString()) }
    }

// Now updatedAvailableTime contains the updated list


}

