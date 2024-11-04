package com.example.rentalbicycle

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import com.example.rentalbicycle.ui.theme.RentalBicycleTheme
import com.razorpay.Checkout
import com.razorpay.PaymentData
import com.razorpay.PaymentResultWithDataListener
import org.json.JSONObject

class MainActivity : ComponentActivity(), PaymentResultWithDataListener {

    //To check if the payment is success or not, whatever the status it will be changed
    val paymentStatus = mutableStateOf<Boolean?>(null)
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RentalBicycleTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    //call functions here
                    Checkout.preload(applicationContext)
                    myapp()

                    //SetDatabase()
                    //homeScreen()
                    //LogOut()
//                    val viewModel: MainViewModel = viewModel()
//                    viewModel.getCycles()
                }
            }
        }
    }
    override fun onPaymentSuccess(p0: String?, p1: PaymentData?) {
        paymentStatus.value=true
        Toast.makeText(this, "Payment Successful", Toast.LENGTH_LONG).show()
    }

    override fun onPaymentError(p0: Int, p1: String?, p2: PaymentData?) {
        //means error has occurred
        paymentStatus.value=false
        Toast.makeText(this, "Error in payment:  ${ p1 }", Toast.LENGTH_LONG).show()
    }
}

fun initPayment(activity: Activity) {
    val co = Checkout()
    co.setKeyID("rzp_test_KYkyKZ1gmJ5kbM")  // Replace with your actual Razorpay Key ID

    try {
        val options = JSONObject()
        options.put("name", "BiCyl")
        options.put("description", "Demoing Charges")
        options.put("image", "http://example.com/image/rzp.jpg")    //Put app logo here
        options.put("theme.color", "#3399cc")   //Razor Interface colour
        options.put("currency", "INR")
        //options.put("order_id", "order_DBJOWzybf0sJbb")   Not mandatory
        options.put("amount", "500") // Amount in currency subunits (e.g., 50000 paise for ₹500)

        //Retry option is enabled and it can try for '4' times.
        val retryObj = JSONObject()
        retryObj.put("enabled", true)
        retryObj.put("max_count", 4)
        options.put("retry", retryObj)

        val prefill = JSONObject()
        prefill.put("email", "realBeast@gmail.com.com")    //Email-id
        prefill.put("contact", "8076949994")

        options.put("prefill", prefill)
        co.open(activity, options)
    } catch (e: Exception) {
        Toast.makeText(activity, "Error in payment: " + e.message, Toast.LENGTH_LONG).show()
        e.printStackTrace()
    }
}

