package com.example.rentalbicycle

import android.app.Application
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application):AndroidViewModel(application) {
    var repository = Repository() //created new repository
    var auth = repository.auth
    var user by mutableStateOf<FirebaseUser?>(null)
    var studentState by repository::studentState

    val scheduleState: StateFlow<schedulesDataState> = repository.scheduleState

    //var scheduleState by repository::scheduleState
    //val availableCycleState: State<getAvailableSlotsStatus> = repository.availabeCycleState
    val availableCycleState: StateFlow<getAvailableSlotsStatus> = repository.availableCycleState


    private val _particularCycleData = MutableStateFlow<Cycle?>(null)
    val particularCycleData: StateFlow<Cycle?> = _particularCycleData

    //var slotStatus = mutableStateOf(availableCycleState.value.allCyclesData)

    init {

        //  whenever value/state of auth changes this code is automatically executed because we have used 'AuthStateListener'
        auth.addAuthStateListener {
            user = auth.currentUser
            user?.let {

                //Main work here to separate admin and student login.TODO

                fetchStudentData(it.uid)
                get_schedule_list_from_schedule_List_String(it.uid)
            }
        }
    }
    @Composable
    fun Demo()
    {
        Log.d("ViewModel",availableCycleState.collectAsState().toString())
    }

    private fun fetchStudentData(uid: String) {
        viewModelScope.launch {
            try {
                auth.currentUser?.let {
                    repository.get_Student_From_Uid(it.uid) // Suspends and waits for result
                }
            } catch (e: Exception) {
                Log.e("Error", "Error occurred: ${e.message}")
            }
        }
    }

    fun get_schedule_list_from_schedule_List_String(uid: String) {
        viewModelScope.launch {
            try {
                auth.currentUser?.let {
                    repository.get_schedule_list_from_schedule_List_String(uid) // Suspends and waits for result
                }
            } catch (e: Exception) {
                Log.e("Error", "Error occurred: ${e.message}")
            }
        }
    }

    fun getAllSlots() {
        viewModelScope.launch {
            Log.d("GetCycle", "calling getCycles")
            repository.getCycles()
            Log.d("GetCycle", "Outside getCycles")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun addNewSchedule(cycle: Cycle, prevTimeSlot:MutableList<String>, newTimeSlot:MutableList<String>) {
        try {
            viewModelScope.launch {
                repository.add_new_schedule(cycle, prevTimeSlot, newTimeSlot)
            }
            Toast.makeText(
                auth.app.applicationContext,
                "Slot Booked Successfully",
                Toast.LENGTH_SHORT
            ).show()
            Log.d("update", "Schedule successfully added")
        } catch (e: Exception) {
            Log.e("update", "Error Occurred while creating a new schedule $e")
        }
    }

    fun fetchParticularCycleDetails(cycleUid:String)
    {
        viewModelScope.launch {
            _particularCycleData.value=repository.fetchParticularCycleDetails(cycleUid)
        }
    }

    fun deleteSchedule(schedule: Schedule,flag:Boolean=true){
        viewModelScope.launch {
            repository.deleteSchedule(schedule,flag)

        }
    }
}





