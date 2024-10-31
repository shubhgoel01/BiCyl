package com.example.rentalbicycle

import android.os.Parcelable
import androidx.compose.ui.graphics.vector.ImageVector
import com.google.firebase.database.PropertyName
import java.io.Serializable
import java.time.LocalDate

/*
    Previously what approach i was using is i was storing user data with email and password, and when user comes and enters the
    roll n umber and password, then i fetch that password and email and login to the firebase.
    bit main problem in this approach is that it contains, lot of security issues, like if database is exploited anyone can access
    main gmail password, else what can i do is remove password field from the data and when the user comes , and enters the roll number and password,
    i check if that roll_number is present in the database if yes, the logins using the stored gmail ans user entered password.


*/


data class Student(
    @PropertyName("name")                  var name: String="",
    @PropertyName("email")                 var email: String="",
    @PropertyName("registrationNumber")    var registrationNumber: String="",
    @PropertyName("branch")                var branch:String=" ",
    @PropertyName("imageURL")              var imageURL:String=""
) {
    // No-argument constructor
    constructor() : this("","","","","")
}

data class user(
    var registrationNumber:String="",    //not needed but we cannot only pass empty list, we need some data
    var scheduleList: MutableList<String> = mutableListOf("Default"),
    val isAdmin: Boolean=false
    //  //basically list of string that will contain the id of Schedule
)
{
    constructor() : this("", mutableListOf("Default"),false)
}

//data class Cycle(
//    val password: String="",
//    val registrationNumber: String="",
//    val list: List<CycleList>
//) {
//    // No-argument constructor
//    constructor() : this("", "", "", "","", emptyList<CycleList>())
//}


data class Cycle(
    @PropertyName("availableTime") var availableTime: MutableList<MutableList<String>> = mutableListOf(
        mutableListOf("Schedule"), // First sublist for header
        mutableListOf("8", "20")   // Second sublist for available times
    ),
    @PropertyName("CycleId") var CycleId: String = "",
    @PropertyName("scheduleList") var scheduleList: List<String> = listOf("Default"),
    @PropertyName("imageURL")   var imageURL: String?=null,
    @PropertyName("location")   var location:String=""
)  : Serializable {
    // No-argument constructor
    constructor() : this(mutableListOf(mutableListOf("Default"),mutableListOf("8", "20")), "", listOf("Default"),null,"")
}



data class Schedule(
    var userUId: String = "",
    var cycleUId: String = "",
    var start_time: String = "",
    var endTime: String = "",
    var scheduleUID: String = "",
    var startDate: String="",  // Change to String type
    var status:String=""
) : Serializable

//data class RollToUidMap(
//    @PropertyName("registration_Number")
//    var registrationNumber: String = ""
//)

data class studentDataState(
    var isLoading:Boolean=true,
    var error:Boolean=false,
    var student:Student= Student(),
    var errorMessage:String?=null
){
    constructor() : this(false, false,Student())
}

data class schedulesDataState(
    var isLoading: Boolean =true,
    var error:Boolean=false,
    var scheduleDataList: MutableList<Schedule> = mutableListOf(),
    var errorMessage:String=""
    )

data class getAvailableSlotsStatus(
    var isLoading: Boolean=true,
    var error: Boolean=false,
    var errorMessage:String="",
    var allCyclesData:MutableList<Cycle> = mutableListOf<Cycle>()
)


data class MenuItem(
    val title: String,
    val icon: ImageVector,
    var isOpen:Boolean=false
)

data class adminData(
    var ID:String="",
    var email:String="",
    var password:String="",
)
{
    constructor() : this("","","")
}

data class admin(
    var ID:String="",
    var history:String="",   //For later update: Basically it will be a list of strings that will contain nodes UID of the history
                            //and there will be a separate node/reference in STORAGE that will contain all history each with UID
                            //so that we can show also show all history and also specific admin history.
    var isAdmin:Boolean=true
)



