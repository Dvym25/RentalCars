package com.example.rentalcars.ActivityPages

import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.example.carrentalapp.Database.BookingDao
import com.example.carrentalapp.Database.CustomerDao
import com.example.carrentalapp.Database.InsuranceDao
import com.example.carrentalapp.Database.Project_Database
import com.example.carrentalapp.Database.VehicleDao
import com.example.carrentalapp.Model.Booking
import com.example.carrentalapp.Model.Customer
import com.example.carrentalapp.Model.Insurance
import com.example.carrentalapp.Model.Vehicle
import com.example.carrentalapp.R
import java.time.temporal.ChronoUnit
import java.util.Calendar

class ViewBookingActivity : AppCompatActivity() {

    private lateinit var back: Button
    private lateinit var returnCar: Button

    // DRIVER DETAILS
    private lateinit var name: TextView
    private lateinit var email: TextView
    private lateinit var phoneNumber: TextView

    // BOOKING SUMMARY
    private lateinit var bookingID: TextView
    private lateinit var vehicleName: TextView
    private lateinit var rate: TextView
    private lateinit var totalDays: TextView
    private lateinit var pickup: TextView
    private lateinit var dropoff: TextView
    private lateinit var insurance: TextView
    private lateinit var insuranceRate: TextView
    private lateinit var totalCost: TextView

    // DATABASE TABLE
    private lateinit var bookingDao: BookingDao
    private lateinit var customerDao: CustomerDao
    private lateinit var vehicleDao: VehicleDao
    private lateinit var insuranceDao: InsuranceDao

    // BOOKING
    private lateinit var booking: Booking
    // INSURANCE
    private lateinit var chosenInsurance: Insurance
    // VEHICLE
    private lateinit var vehicle: Vehicle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_booking)

        initComponents()
        listenHandler()
        displayCustomerInformation()
        displaySummary()
        displayTotalCost()
    }

    private fun initComponents() {
        back = findViewById(R.id.back)

        // DRIVER DETAILS
        name = findViewById(R.id.name)
        email = findViewById(R.id.email)
        phoneNumber = findViewById(R.id.phoneNumber)

        // BOOKING SUMMARY
        vehicleName = findViewById(R.id.vehicleName)
        rate = findViewById(R.id.rate)
        totalDays = findViewById(R.id.totalDays)
        pickup = findViewById(R.id.pickup)
        dropoff = findViewById(R.id.dropoff)

        // INSURANCE TYPE
        insurance = findViewById(R.id.insurance)
        insuranceRate = findViewById(R.id.insuranceRate)

        // TOTAL COST
        totalCost = findViewById(R.id.totalCost)

        // DATABASE TABLE
        val database = Room.databaseBuilder(applicationContext, Project_Database::class.java, "car_rental_db")
            .allowMainThreadQueries()
            .build()

        bookingDao = database.bookingDao()
        customerDao = database.customerDao()
        vehicleDao = database.vehicleDao()
        insuranceDao = database.insuranceDao()

        // GET BOOKING OBJECT WHICH WAS PASSED FROM PREVIOUS PAGE
        val bookingID = intent.getStringExtra("BOOKINGID")?.toInt() ?: return
        booking = bookingDao.findBooking(bookingID)
        chosenInsurance = insuranceDao.findInsurance(booking.insuranceID)
        vehicle = vehicleDao.findVehicle(booking.vehicleID)

        this.bookingID = findViewById(R.id.bookingID)
    }

    private fun listenHandler() {
        back.setOnClickListener {
            finish()
        }
    }

    private fun displayCustomerInformation() {
        val customer = customerDao.findUser(booking.customerID)
        // DISPLAY DRIVER INFO
        name.text = customer.fullName
        email.text = customer.email
        phoneNumber.text = customer.phoneNumber

        bookingID.text = "BookingID: ${booking.bookingID}"
    }

    private fun displaySummary() {
        vehicleName.text = vehicle.fullTitle()
        rate.text = "$${vehicle.price}/Day"
        totalDays.text = "${getDayDifference(booking.pickupDate, booking.returnDate)} Days"
        pickup.text = booking.pickupTime
        dropoff.text = booking.returnTime

        insurance.text = chosenInsurance.coverageType
        insuranceRate.text = "$${chosenInsurance.cost}"
    }

    private fun displayTotalCost() {
        val cost = calculateTotalCost()
        totalCost.text = "$$cost"
    }

    private fun getDayDifference(start: Calendar, end: Calendar): Long {
        return ChronoUnit.DAYS.between(start.toInstant(), end.toInstant()) + 2
    }

    private fun calculateTotalCost(): Double {
        val days = getDayDifference(booking.pickupDate, booking.returnDate)
        val vehicleRate = vehicle.price
        val insuranceRate = chosenInsurance.cost

        return (days * vehicleRate) + insuranceRate
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val homepage = Intent(applicationContext, UserViewActivity::class.java)
        homepage.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) // Will clear out your activity history stack till now
        startActivity(homepage)
    }
}
