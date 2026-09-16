package com.example.salarybox

import android.Manifest
import androidx.activity.compose.setContent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.salarybox.ui.theme.SalaryBoxTheme
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

private const val ADMIN_USER = "admin"
private const val ADMIN_PASS = "admin123"

// Assignment data model.
data class Staff(
    val name: String,
    val employeeId: String,
    val monthlySalary: Int,
    val username: String,
    val password: String,
    var enrolledFacePath: String? = null
)

data class AttendanceRecord(
    val staffName: String,
    val employeeId: String,
    val date: String,
    val time: String,
    val latitude: Double,
    val longitude: Double,
    val selfiePath: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                1001
            )
        }

        setContent {
            SalaryBoxTheme {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    SalaryBoxApp()
                }
            }
        }
    }
}

@Composable
fun SalaryBoxApp() {
    var screen by remember { mutableStateOf("login") }
    var selectedStaff by remember { mutableStateOf<Staff?>(null) }
    var loggedInStaff by remember { mutableStateOf<Staff?>(null) }
    var toastMessage by remember { mutableStateOf("") }

    val staffList = remember {
        mutableStateListOf(
            Staff("Rahul", "EMP001", 30000, "rahul", "rahul123"),
            Staff("Aman", "EMP002", 25000, "aman", "aman123"),
            Staff("Tanmay", "EMP003", 30000, "tanmay", "tanmay123")
        )
    }
    val attendance = remember { mutableStateListOf<AttendanceRecord>() }

    fun logout() {
        selectedStaff = null
        loggedInStaff = null
        screen = "login"
    }

    when (screen) {
        "login" -> LoginScreen(
            staffList = staffList,
            onAdminLogin = { screen = "admin" },
            onStaffLogin = {
                loggedInStaff = it
                screen = "staff"
            },
            onError = { toastMessage = it }
        )

        "admin" -> AdminHomeScreen(
            onViewStaff = { screen = "staffList" },
            onAddStaff = { screen = "addStaff" },
            onAttendance = { screen = "attendanceHistory" },
            onLogout = ::logout
        )

        "staffList" -> StaffListScreen(
            staffList = staffList,
            onSelect = {
                selectedStaff = it
                screen = "staffProfile"
            },
            onBack = { screen = "admin" }
        )

        "addStaff" -> AddStaffScreen(
            existingUsernames = staffList.map { it.username },
            existingEmployeeIds = staffList.map { it.employeeId },
            onBack = { screen = "admin" },
            onSave = { name, employeeId, salary, username, password ->
                staffList.add(Staff(name, employeeId, salary, username, password))
                screen = "staffList"
            }
        )

        "staffProfile" -> selectedStaff?.let { staff ->
            StaffProfileScreen(
                staff = staff,
                history = attendance.filter { it.employeeId == staff.employeeId },
                onEnroll = { screen = "faceEnroll" },
                onHistory = { screen = "staffHistory" },
                onBack = { screen = "staffList" }
            )
        }

        "faceEnroll" -> selectedStaff?.let { staff ->
            FaceEnrollScreen(
                staff = staff,
                onEnrolled = { path ->
                    staff.enrolledFacePath = path
                    screen = "staffProfile"
                },
                onBack = { screen = "staffProfile" }
            )
        }

        "staffHistory" -> selectedStaff?.let { staff ->
            AttendanceListScreen(
                title = "${staff.name} - Attendance",
                history = attendance.filter { it.employeeId == staff.employeeId },
                onBack = { screen = "staffProfile" }
            )
        }

        "attendanceHistory" -> AttendanceListScreen(
            title = "Attendance History",
            history = attendance,
            onBack = { screen = "admin" }
        )

        "staff" -> loggedInStaff?.let { staff ->
            StaffHomeScreen(
                staff = staff,
                history = attendance.filter { it.employeeId == staff.employeeId },
                onMark = { screen = "markAttendance" },
                onHistory = { screen = "staffSelfHistory" },
                onLogout = ::logout
            )
        }

        "staffSelfHistory" -> loggedInStaff?.let { staff ->
            AttendanceListScreen(
                title = "My Attendance",
                history = attendance.filter { it.employeeId == staff.employeeId },
                onBack = { screen = "staff" }
            )
        }

        "markAttendance" -> loggedInStaff?.let { staff ->
            MarkAttendanceScreen(
                staff = staff,
                history = attendance,
                onSuccess = { record ->
                    attendance.add(record)
                    screen = "staff"
                },
                onBack = { screen = "staff" },
                onError = { toastMessage = it }
            )
        }
    }

    if (toastMessage.isNotBlank()) {
        Toast.makeText(LocalContext.current, toastMessage, Toast.LENGTH_LONG).show()
        toastMessage = ""
    }
}

@Composable
fun LoginScreen(
    staffList: List<Staff>,
    onAdminLogin: () -> Unit,
    onStaffLogin: (Staff) -> Unit,
    onError: (String) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        Modifier.fillMaxSize().padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("SalaryBox", style = MaterialTheme.typography.headlineLarge)
        Text("Attendance & Salary Management")
        Spacer(Modifier.height(28.dp))
        OutlinedTextField(username, { username = it }, label = { Text("Username") }, singleLine = true)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            password,
            { password = it },
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation()
        )
        Spacer(Modifier.height(18.dp))
        Button(
            onClick = {
                when {
                    username == ADMIN_USER && password == ADMIN_PASS -> onAdminLogin()
                    else -> staffList.firstOrNull { it.username == username && it.password == password }
                        ?.let(onStaffLogin)
                        ?: onError("Invalid username or password")
                }
            },
            modifier = Modifier.width(230.dp)
        ) { Text("Login") }
    }
}

@Composable
fun AdminHomeScreen(
    onViewStaff: () -> Unit,
    onAddStaff: () -> Unit,
    onAttendance: () -> Unit,
    onLogout: () -> Unit
) {
    SimpleScreen("Admin Dashboard", "Staff Management") {
        Button(onViewStaff, Modifier.fillMaxWidth()) { Text("View Staff") }
        Button(onAddStaff, Modifier.fillMaxWidth()) { Text("Add Staff") }
        Button(onAttendance, Modifier.fillMaxWidth()) { Text("Attendance History") }
        TextButton(onLogout) { Text("Logout") }
    }
}

@Composable
fun StaffListScreen(staffList: List<Staff>, onSelect: (Staff) -> Unit, onBack: () -> Unit) {
    SimpleScreen("Staff List") {
        staffList.forEach { staff ->
            Card(Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
                Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text(staff.name, style = MaterialTheme.typography.titleMedium)
                        Text("Employee ID: ${staff.employeeId}")
                    }
                    TextButton({ onSelect(staff) }) { Text("Open") }
                }
            }
        }
        TextButton(onBack) { Text("Back") }
    }
}

@Composable
fun AddStaffScreen(
    existingUsernames: List<String>,
    existingEmployeeIds: List<String>,
    onBack: () -> Unit,
    onSave: (String, String, Int, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var employeeId by remember { mutableStateOf("") }
    var salary by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    SimpleScreen("Add Staff") {
        OutlinedTextField(name, { name = it }, label = { Text("Staff Name") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(employeeId, { employeeId = it }, label = { Text("Employee ID") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(salary, { salary = it }, label = { Text("Monthly Salary") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(username, { username = it }, label = { Text("Username") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(password, { password = it }, label = { Text("Password") }, modifier = Modifier.fillMaxWidth(), visualTransformation = PasswordVisualTransformation())
        if (error.isNotBlank()) Text(error, color = MaterialTheme.colorScheme.error)
        Button({
            val cleanId = employeeId.trim()
            val cleanUser = username.trim()
            val amount = salary.toIntOrNull()
            error = when {
                name.isBlank() || cleanId.isBlank() || cleanUser.isBlank() || password.isBlank() -> "Fill all fields"
                cleanId in existingEmployeeIds -> "Employee ID already exists"
                cleanUser in existingUsernames -> "Username already exists"
                amount == null || amount <= 0 -> "Enter a valid salary"
                else -> ""
            }
            if (error.isBlank()) onSave(name.trim(), cleanId, amount!!, cleanUser, password)
        }, Modifier.fillMaxWidth()) { Text("Save Staff") }
        TextButton(onBack) { Text("Back") }
    }
}

@Composable
fun StaffProfileScreen(
    staff: Staff,
    history: List<AttendanceRecord>,
    onEnroll: () -> Unit,
    onHistory: () -> Unit,
    onBack: () -> Unit
) {
    SimpleScreen("Staff Profile") {
        Text(staff.name, style = MaterialTheme.typography.headlineSmall)
        Text("Employee ID: ${staff.employeeId}")
        Text("Monthly Salary: ₹${staff.monthlySalary}")
        Text("Face enrolled: ${if (staff.enrolledFacePath != null) "Yes" else "No"}")
        Text("Attendance records: ${history.size}")
        Button(onEnroll, Modifier.fillMaxWidth()) { Text(if (staff.enrolledFacePath == null) "Enrol Face" else "Re-Enrol Face") }
        Button(onHistory, Modifier.fillMaxWidth()) { Text("Attendance History") }
        TextButton(onBack) { Text("Back") }
    }
}

/**
 * Camera contract that asks the device camera app for a front-camera selfie.
 *
 * Some emulator camera implementations return RESULT_CANCELED even after the
 * user presses the check/confirm button, but still put the captured thumbnail
 * in Intent extras. We therefore parse the bitmap directly and do not reject
 * it only because resultCode is RESULT_CANCELED.
 */
private class FrontCameraPreviewContract : ActivityResultContract<Unit, Bitmap?>() {
    override fun createIntent(context: Context, input: Unit): Intent {
        return Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra("android.intent.extras.CAMERA_FACING", 1)
            putExtra("android.intent.extra.USE_FRONT_CAMERA", true)
            putExtra("android.intent.extra.QUICK_CAPTURE", false)
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Bitmap? {
        return intent?.extras?.get("data") as? Bitmap
    }
}

@Composable
fun FaceEnrollScreen(
    staff: Staff,
    onEnrolled: (String) -> Unit,
    onBack: () -> Unit
) {
    var status by remember {
        mutableStateOf("Tap below to open the front camera.")
    }

    var busy by remember {
        mutableStateOf(false)
    }

    val context = LocalContext.current

    val cameraLauncher =
        rememberLauncherForActivityResult(
            contract = FrontCameraPreviewContract()
        ) { bitmap ->

            busy = false

            if (bitmap == null) {
                status = "No photo was returned. Tap Open Front Camera and capture again."
                return@rememberLauncherForActivityResult
            }

            status = "Checking captured photo for a face..."

            detectSingleFace(bitmap) { ok, message ->
                if (!ok) {
                    status = message
                } else {
                    val path = saveBitmap(
                        context,
                        bitmap,
                        "enrolled_${staff.employeeId}.jpg"
                    )

                    onEnrolled(path)
                }
            }
        }

    SimpleScreen(title = "Face Enrolment") {

        Text("Staff: ${staff.name}")

        Text("The front camera will capture one enrolment selfie.")

        Text(status)

        Button(
            enabled = !busy,
            onClick = {
                busy = true
                status = "Opening camera..."
                cameraLauncher.launch(Unit)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Open Front Camera")
        }

        TextButton(
            onClick = onBack
        ) {
            Text("Back")
        }
    }
}

@Composable
fun StaffHomeScreen(
    staff: Staff,
    history: List<AttendanceRecord>,
    onMark: () -> Unit,
    onHistory: () -> Unit,
    onLogout: () -> Unit
) {
    val today = today()
    val marked = history.any { it.date == today }
    SimpleScreen("Staff Dashboard", staff.name) {
        Text("Welcome")
        Button(enabled = !marked && staff.enrolledFacePath != null, onClick = onMark, modifier = Modifier.fillMaxWidth()) {
            Text(when {
                staff.enrolledFacePath == null -> "Face Not Enrolled"
                marked -> "Attendance Marked"
                else -> "Mark Attendance"
            })
        }
        if (staff.enrolledFacePath == null) Text("Ask admin to enrol your face before marking attendance.")
        Text("Today's Date: $today")
        Text("Attendance Records: ${history.size}")
        Button(onHistory, Modifier.fillMaxWidth()) { Text("Attendance History") }
        TextButton(onLogout) { Text("Logout") }
    }
}

@Composable
fun MarkAttendanceScreen(
    staff: Staff,
    history: List<AttendanceRecord>,
    onSuccess: (AttendanceRecord) -> Unit,
    onBack: () -> Unit,
    onError: (String) -> Unit
) {
    var status by remember { mutableStateOf("Tap Mark Attendance to capture the selfie.") }
    var busy by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val locationManager = remember { context.getSystemService(Context.LOCATION_SERVICE) as LocationManager }

    val cameraLauncher = rememberLauncherForActivityResult(FrontCameraPreviewContract()) { bitmap ->
        busy = false
        if (bitmap == null) {
            status = "No photo was returned. Tap Mark Attendance and capture again."
            return@rememberLauncherForActivityResult
        }
        val enrolledPath = staff.enrolledFacePath
        if (enrolledPath == null) {
            onError("Face is not enrolled for this staff member.")
            return@rememberLauncherForActivityResult
        }
        val enrolled = BitmapFactory.decodeFile(enrolledPath)
        if (enrolled == null) {
            onError("Enrolled face image could not be loaded.")
            return@rememberLauncherForActivityResult
        }
        status = "Checking face..."
        compareFaces(enrolled, bitmap) { matched, message ->
            if (!matched) {
                status = message
                return@compareFaces
            }
            status = "Face matched. Getting location..."
            getCurrentLocation(context, locationManager) { location ->
                if (location == null) {
                    status = "Location unavailable. Turn on GPS and try again."
                    return@getCurrentLocation
                }
                val now = Date()
                val record = AttendanceRecord(
                    staffName = staff.name,
                    employeeId = staff.employeeId,
                    date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(now),
                    time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(now),
                    latitude = location.latitude,
                    longitude = location.longitude,
                    selfiePath = saveBitmap(context, bitmap, "attendance_${staff.employeeId}_${now.time}.jpg")
                )
                onSuccess(record)
            }
        }
    }

    SimpleScreen("Mark Attendance") {
        Text("Staff: ${staff.name}")
        Text(status)
        Button(enabled = !busy && history.none { it.employeeId == staff.employeeId && it.date == today() }, onClick = {
            if (staff.enrolledFacePath == null) {
                onError("Please enrol face first.")
                return@Button
            }
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                onError("Camera permission is required. Please allow it and try again.")
                return@Button
            }
            if (!hasLocationPermission(context)) {
                onError("Location permission is required. Please allow it and try again.")
                return@Button
            }
            busy = true
            cameraLauncher.launch(Unit)
        }, modifier = Modifier.fillMaxWidth(), content = {
            Text("Capture Selfie & Mark Attendance")
        })
        Text("Attendance is saved only after face match + GPS location.")
        TextButton(onBack) { Text("Back") }
    }
}

@Composable
fun AttendanceListScreen(title: String, history: List<AttendanceRecord>, onBack: () -> Unit) {
    SimpleScreen(title) {
        if (history.isEmpty()) {
            Text("No attendance records.")
        } else {
            history.asReversed().forEachIndexed { index, record ->
                Card(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                    Column(Modifier.padding(14.dp)) {
                        Text("${index + 1}. ${record.staffName}", style = MaterialTheme.typography.titleMedium)
                        Text("Employee ID: ${record.employeeId}")
                        Text("Date: ${record.date}")
                        Text("Time: ${record.time}")
                        Text("Latitude: ${"%.6f".format(Locale.US, record.latitude)}")
                        Text("Longitude: ${"%.6f".format(Locale.US, record.longitude)}")
                        val image = remember(record.selfiePath) { BitmapFactory.decodeFile(record.selfiePath) }
                        if (image != null) {
                            Spacer(Modifier.height(8.dp))
                            Image(image.asImageBitmap(), "Attendance selfie", Modifier.size(100.dp))
                        }
                    }
                }
            }
        }
        TextButton(onBack) { Text("Back") }
    }
}

@Composable
fun SimpleScreen(title: String, subtitle: String? = null, content: @Composable () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(title, style = MaterialTheme.typography.headlineMedium)
        if (subtitle != null) Text(subtitle)
        Spacer(Modifier.height(20.dp))
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) { content() }
    }
}

private fun today(): String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

private fun saveBitmap(context: Context, bitmap: Bitmap, name: String): String {
    val file = File(context.filesDir, name)
    FileOutputStream(file).use { out -> bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out) }
    return file.absolutePath
}

private fun detectSingleFace(bitmap: Bitmap, callback: (Boolean, String) -> Unit) {
    val options = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
        .build()
    val detector = FaceDetection.getClient(options)
    detector.process(InputImage.fromBitmap(bitmap, 0))
        .addOnSuccessListener { faces ->
            detector.close()
            when {
                faces.isEmpty() -> callback(false, "No face detected. Look directly at the camera and try again.")
                faces.size > 1 -> callback(false, "Multiple faces detected. Only one person should be in the camera frame.")
                else -> callback(true, "Face detected.")
            }
        }
        .addOnFailureListener {
            detector.close()
            callback(false, "Face detection failed: ${it.message ?: "unknown error"}")
        }
}

private fun compareFaces(enrolled: Bitmap, current: Bitmap, callback: (Boolean, String) -> Unit) {
    val options = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
        .build()
    val detector = FaceDetection.getClient(options)

    detector.process(InputImage.fromBitmap(enrolled, 0))
        .addOnSuccessListener { enrolledFaces ->
            if (enrolledFaces.size != 1) {
                detector.close()
                callback(false, "The enrolled image does not contain exactly one face.")
                return@addOnSuccessListener
            }

            detector.process(InputImage.fromBitmap(current, 0))
                .addOnSuccessListener { currentFaces ->
                    detector.close()
                    if (currentFaces.size != 1) {
                        callback(false, "Exactly one face must be visible for attendance.")
                        return@addOnSuccessListener
                    }

                    val score = faceGeometryScore(enrolledFaces[0], currentFaces[0])
                    // Lightweight on-device assignment demo. This is not biometric-grade recognition.
                    if (score >= 0.62) {
                        callback(true, "Face matched")
                    } else {
                        callback(false, "Face did not match the enrolled face. Try again with similar lighting and position.")
                    }
                }
                .addOnFailureListener {
                    detector.close()
                    callback(false, "Current face detection failed: ${it.message ?: "unknown error"}")
                }
        }
        .addOnFailureListener {
            detector.close()
            callback(false, "Enrolled face detection failed: ${it.message ?: "unknown error"}")
        }
}

private fun faceGeometryScore(a: Face, b: Face): Double {
    val arA = a.boundingBox.width().toDouble() / max(1, a.boundingBox.height())
    val arB = b.boundingBox.width().toDouble() / max(1, b.boundingBox.height())
    val ratioScore = 1.0 - min(1.0, abs(arA - arB))
    val yawScore = 1.0 - min(1.0, abs(a.headEulerAngleY - b.headEulerAngleY) / 45.0)
    val pitchScore = 1.0 - min(1.0, abs(a.headEulerAngleX - b.headEulerAngleX) / 45.0)
    val rollScore = 1.0 - min(1.0, abs(a.headEulerAngleZ - b.headEulerAngleZ) / 45.0)
    return ratioScore * 0.35 + yawScore * 0.25 + pitchScore * 0.20 + rollScore * 0.20
}

private fun hasLocationPermission(context: Context): Boolean =
    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

private fun getCurrentLocation(
    context: Context,
    manager: LocationManager,
    callback: (Location?) -> Unit
) {
    if (!hasLocationPermission(context)) {
        callback(null)
        return
    }

    val provider = when {
        manager.isProviderEnabled(LocationManager.GPS_PROVIDER) ->
            LocationManager.GPS_PROVIDER

        manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) ->
            LocationManager.NETWORK_PROVIDER

        else -> null
    }

    if (provider == null) {
        callback(null)
        return
    }

    try {
        val last = manager.getLastKnownLocation(provider)

        if (last != null) {
            callback(last)
            return
        }

        manager.getCurrentLocation(
            provider,
            null,
            ContextCompat.getMainExecutor(context)
        ) { location ->
            callback(location)
        }
    } catch (_: SecurityException) {
        callback(null)
    }
}
