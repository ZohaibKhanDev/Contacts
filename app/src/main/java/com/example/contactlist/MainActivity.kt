package com.example.contactlist

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Message
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.NavType.Companion.StringType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.compose.AsyncImage
import com.example.contactlist.ui.theme.ContactListTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    var contactItems: List<Triple<String, String, String?>> by mutableStateOf(emptyList())
    var darkMode by mutableStateOf(false)

    @OptIn(ExperimentalFoundationApi::class)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            ContactListTheme(darkTheme = darkMode) {
                Scaffold {

                    val navController = rememberNavController()

                    NavHost(navController = navController, startDestination = Screen.Phone.route) {
                        composable(Screen.Phone.route) {
                            PhoneScreen(navController)
                        }

                        composable(Screen.Contacts.route) {
                            ContactList(contactItems, navController)
                        }



                        composable(Screen.ContatsDetail.route + "/{name}/{num}",
                            arguments = listOf(navArgument("name") {
                                type = StringType
                            }, navArgument("num") {
                                type = StringType
                            })) {
                            val name = it.arguments?.getString("name")
                            val num = it.arguments?.getString("num")
                            ContactDetail(navController = navController, num = num, name = name)
                        }
                    }
                }
            }
        }
        requestContactsPermission()
    }

    private fun requestContactsPermission() {
        permissionGaranted(this, Manifest.permission.READ_CONTACTS) { granted ->
            if (granted) {
                contactItems = getContactList()
            } else {
                registerActivityResult.launch(Manifest.permission.READ_CONTACTS)
            }
        }
    }

    val registerActivityResult =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                contactItems = getContactList()
            }
        }

    @SuppressLint("Range", "Recycle")
    private fun getContactList(): List<Triple<String, String, String?>> {
        val contactsList = mutableListOf<Triple<String, String, String?>>()
        val contentResolver = contentResolver
        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null
        )
        cursor?.use {
            while (it.moveToNext()) {
                val name = it.getString(it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                val number =
                    it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                val picUri =
                    it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Photo.PHOTO_URI))
                contactsList.add(Triple(name, number, picUri))
            }
        }
        return contactsList
    }

    private fun toggleDarkMode() {
        darkMode = !darkMode
    }


    @OptIn(ExperimentalMaterial3Api::class)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @Composable
    fun PhoneScreen(navController: NavController) {
        var phone by remember {
            mutableStateOf(false)
        }

        var phonetext by remember {
            mutableStateOf("")
        }
        val context = LocalContext.current
        val requestPermissionLauncher =
            rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) { isGaranted: Boolean ->
                if (isGaranted) {
                    initiateCall(context, phonetext)
                }

            }


        Scaffold(topBar = {
            LargeTopAppBar(title = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Text(
                            text = "Phone",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.clickable { },
                            color = Color(0XFF00961e)
                        )
                        Text(text = "Contacts",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.clickable { navController.navigate(Screen.Contacts.route) })
                    }

                }


            }, colors = TopAppBarDefaults.topAppBarColors(Color.White), actions = {
                Icon(imageVector = Icons.Outlined.MoreVert, contentDescription = "")
            }, navigationIcon = {
                Text(text = "Edit",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { })

            })
        }, floatingActionButton = {

            Card(
                modifier = Modifier.size(50.dp),
                colors = CardDefaults.cardColors(Color(0XFF00961e)),
                shape = CircleShape,
                elevation = CardDefaults.cardElevation(5.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "",
                    modifier = Modifier
                        .padding(top = 13.dp)
                        .align(Alignment.CenterHorizontally)
                        .clickable { phone = !phone },
                    tint = Color.White
                )
            }

        }) {
            val bottom = rememberModalBottomSheetState()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
            ) {
                AnimatedVisibility(
                    visible = phone
                ) {

                    ModalBottomSheet(
                        onDismissRequest = { bottom },
                        modifier = Modifier.height(111.dp),
                        containerColor = Color.White
                    ) {
                        Card(elevation = CardDefaults.cardElevation(1.dp)) {
                            TextField(
                                value = phonetext,
                                onValueChange = {
                                    phonetext = it
                                },
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .fillMaxWidth(),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                    focusedIndicatorColor = Color.White,
                                    unfocusedIndicatorColor = Color.White
                                ),
                                singleLine = true,
                                keyboardActions = KeyboardActions(onDone = {
                                    CoroutineScope(Dispatchers.Main).launch {
                                        bottom.hide()
                                    }
                                }),
                                keyboardOptions = KeyboardOptions(
                                    imeAction = ImeAction.Done,
                                    keyboardType = KeyboardType.Number,
                                    autoCorrect = true
                                ),
                                enabled = true,
                                trailingIcon = {
                                    if (phonetext.length > 11) {
                                        Icon(imageVector = Icons.Default.Call,
                                            contentDescription = "",
                                            Modifier.clickable {
                                                if (ActivityCompat.checkSelfPermission(
                                                        context, Manifest.permission.CALL_PHONE
                                                    ) == PackageManager.PERMISSION_GRANTED
                                                ) {
                                                    initiateCall(context, phonetext)
                                                } else {
                                                    requestPermissionLauncher.launch(Manifest.permission.CALL_PHONE)
                                                }
                                            })
                                    } else {


                                        Icon(
                                            imageVector = Icons.Default.Call,
                                            contentDescription = "",
                                            tint = Color.Gray.copy(alpha = 0.90f)
                                        )
                                    }


                                },
                                leadingIcon = {
                                    if (phonetext.length > 11) {
                                        Icon(imageVector = Icons.Outlined.Add,
                                            contentDescription = "",
                                            modifier = Modifier.clickable {
                                                navController.navigate(
                                                    Screen.Contacts.route + "/${phonetext}"
                                                )
                                            })
                                    } else {
                                        Icon(
                                            imageVector = Icons.Outlined.Add,
                                            contentDescription = "",
                                            tint = Color.Gray.copy(alpha = 0.90f)
                                        )
                                    }
                                },

                                )
                        }

                    }


                }
            }
        }

    }

}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ContactList(
    contact: List<Triple<String, String, String?>>,
    navController: NavController,
) {
    var textField by remember {
        mutableStateOf("")
    }/*  val filteredContacts = if (textField.isBlank()) {
          contact
      } else {
          contact.filter {
              it.first.contains(textField, ignoreCase = true) || it.second.contains(textField)
          }
      }*/
    var floatingButton by remember {
        mutableStateOf(false)
    }
    val filteredContacts = if (textField.isBlank()) {
        contact
    } else {
        contact.filter {
            it.first.contains(textField, ignoreCase = true) || it.second.contains(textField)
        }
    }
    Scaffold(topBar = {
        LargeTopAppBar(title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Text(text = "Phone",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable { navController.navigate(Screen.Phone.route) })
                    Text(
                        text = "Contacts",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0XFF00961e)
                    )
                }


                TextField(
                    value = textField,
                    onValueChange = {
                        textField = it

                    },
                    textStyle = TextStyle(
                        fontSize = 14.sp
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0XFF767680).copy(alpha = 0.12f),
                        unfocusedContainerColor = Color(0XFF767680).copy(alpha = 0.12f),
                        focusedLeadingIconColor = Color.Black,
                        unfocusedLeadingIconColor = Color.Black,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .height(50.dp)
                        .width(300.dp)
                        .align(Alignment.CenterHorizontally),
                    shape = RoundedCornerShape(13.dp),
                    placeholder = {
                        Text(
                            text = "Search among 16 contact(s)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "",
                            Modifier.size(23.dp)
                        )
                    },
                    singleLine = true,

                    )
            }


        }, colors = TopAppBarDefaults.topAppBarColors(Color.White), actions = {
            Icon(imageVector = Icons.Outlined.MoreVert, contentDescription = "")
        }, navigationIcon = {
            Text(text = "Edit", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        })
    }, floatingActionButton = {

        Card(
            modifier = Modifier
                .size(50.dp)
                .clickable { floatingButton = !floatingButton },
            colors = CardDefaults.cardColors(Color.White),
            shape = CircleShape,
            elevation = CardDefaults.cardElevation(5.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "",
                modifier = Modifier
                    .padding(top = 13.dp)
                    .align(Alignment.CenterHorizontally)
            )
        }

    }) {
        LazyColumn(
            modifier = Modifier
                .padding(start = 50.dp, top = it.calculateTopPadding())
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            items(filteredContacts) { contactInfo ->
                var (name, number, picUri) = contactInfo

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp)
                        .clickable {
                            navController.navigate(Screen.ContatsDetail.route + "/$number/$name")
                        },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    if (picUri != null) {
                        AsyncImage(
                            model = picUri,
                            contentDescription = "",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .background(color = randomColor(), shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = name.take(1).toUpperCase(),
                                color = Color.Black,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (floatingButton) {
                        var firstName by remember {
                            mutableStateOf("")
                        }

                        var number by remember {
                            mutableStateOf("")
                        }

                        val scope = rememberCoroutineScope()
                        val context= LocalContext.current
                        AlertDialog(
                            onDismissRequest = { floatingButton },
                            confirmButton = {

                                Text(text = "Save",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.clickable { navController.navigate(Screen.Contacts.route)
                                        scope.launch {
                                        Toast.makeText(context,"Contact Saved", Toast.LENGTH_SHORT).show()

                                        }
                                     })
                            },
                            title = {
                                OutlinedTextField(value = firstName, onValueChange = {
                                    firstName= it
                                }, placeholder = {
                                    Text(text = "Enter Name")
                                }, singleLine = true,
                                    textStyle = TextStyle(fontSize = 20.sp)
                                    )
                            },
                            text = {
                                OutlinedTextField(value = number, onValueChange = {
                                    number = it
                                }, placeholder = {
                                    Text(text = "Enter Number")
                                }, singleLine = true, textStyle = TextStyle(
                                    fontSize = 20.sp
                                ))
                            }
                        )
                    }

                    Column {

                        /* Text(text = number)*/

                        Text(text = name.replace("(", ""))
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ContactDetail(navController: NavController, num: String?, name: String?) {

    val context = LocalContext.current/* val requestPermissionLauncher = rememberLauncherForActivityResult(
         contract = ActivityResultContracts.RequestPermission()
     ) { isGranted: Boolean ->
         if (isGranted) {
             if (name != null) {
                 initiateCall(context, name)
             }
         }
     }*/

    val requestPermissionLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) { isGaranted: Boolean ->

            if (isGaranted) {
                if (name != null) {
                    initiateCall(context, name)
                }
            }
        }

    var star by remember {
        mutableStateOf(false)
    }

    Scaffold(topBar = {
        TopAppBar(title = { }, navigationIcon = {
            Icon(imageVector = Icons.Default.ArrowBack,
                contentDescription = "",
                modifier = Modifier.clickable { navController.popBackStack() })
        }, colors = TopAppBarDefaults.topAppBarColors(Color.White),

            actions = {
                Icon(imageVector = if (star) Icons.Filled.Star else Icons.Outlined.StarOutline,
                    contentDescription = "",
                    modifier = Modifier.clickable { star = !star })
                Spacer(modifier = Modifier.width(20.dp))
                Icon(
                    imageVector = Icons.Outlined.MoreVert,
                    contentDescription = "",
                )


            })
    }) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it.calculateTopPadding()),
            verticalArrangement = Arrangement.spacedBy(
                15.dp
            ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .background(color = randomColor(), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                num?.take(1)?.toUpperCase(Locale.ROOT)?.let { it1 ->
                    Text(
                        text = it1,
                        color = Color.Black,
                        fontSize = 50.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

            }
            num?.replace("(", "")?.let { it1 ->
                Text(
                    text = it1, fontSize = 24.sp, fontWeight = FontWeight.SemiBold
                )
            }



            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray.copy(alpha = 0.30f)),
                    contentAlignment = Alignment.Center
                ) {

                    Icon(imageVector = Icons.Default.Call,
                        contentDescription = "",
                        modifier = Modifier.clickable {
                                /*   if (ContextCompat.checkSelfPermission(
                                           context,
                                           Manifest.permission.CALL_PHONE
                                       ) == PackageManager.PERMISSION_GRANTED
                                   ) {
                                       if (num != null) {
                                           initiateCall(context, num)
                                       }
                                   } else {
                                       requestPermissionLauncher.launch(Manifest.permission.CALL_PHONE)
                                   }*/
                                if (ContextCompat.checkSelfPermission(
                                        context, Manifest.permission.CALL_PHONE
                                    ) == PackageManager.PERMISSION_GRANTED
                                ) {

                                    if (name != null) {
                                        initiateCall(context, name)
                                    }

                                } else {
                                    requestPermissionLauncher.launch(Manifest.permission.CALL_PHONE)
                                }


                            }

                    )
                }

                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray.copy(alpha = 0.30f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Outlined.Message, contentDescription = "")
                }

                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray.copy(alpha = 0.30f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Outlined.Videocam, contentDescription = "")
                }
            }

            Divider()

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {

                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray.copy(alpha = 0.30f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Outlined.Message, contentDescription = "")
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(9.dp)
                ) {


                    Icon(imageVector = Icons.Outlined.Call,
                        contentDescription = "",
                        modifier = Modifier
                            .padding(end = 7.dp, bottom = 5.dp)
                            .size(24.dp)
                            .clickable {
                                if (ActivityCompat.checkSelfPermission(
                                        context, Manifest.permission.CALL_PHONE
                                    ) == PackageManager.PERMISSION_GRANTED
                                ) {
                                    initiateCall(
                                        context, name
                                    )
                                } else {
                                    requestPermissionLauncher.launch(Manifest.permission.CALL_PHONE)
                                }

                            })
                    Column(
                        modifier = Modifier.wrapContentWidth(),
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = name?.replace("()", "").toString(),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.W400
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Text(
                                text = "Mobile | ",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Gray
                            )
                            Text(
                                text = "Country",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Gray
                            )
                        }
                    }


                }
            }
        }
    }
}


private fun initiateCall(context: Context, name: String?) {
    val intent = Intent(Intent.ACTION_CALL).apply {
        data = Uri.parse("tel:$name")
    }

    context.startActivity(intent)
}

inline fun randomColor(): Color {
    val color = Color(
        red = Random.nextInt(256), green = Random.nextInt(256), blue = Random.nextInt(256)
    ).copy(alpha = 0.5f)
    return color
}

inline fun permissionGaranted(context: Context, permission: String, call: (Boolean) -> Unit) {
    if (ContextCompat.checkSelfPermission(
            context, permission
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        call.invoke(true)
    } else {
        call.invoke(false)
    }
}


sealed class Screen(
    val route: String,

    ) {
    object Phone : Screen("Phone")
    object Contacts : Screen("Contacts")

    object ContatsDetail : Screen("ContactDetail")
}