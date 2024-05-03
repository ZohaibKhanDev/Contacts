package com.example.contactlist

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Message
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.NavType.Companion.StringType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.contactapp.R
import com.example.contactlist.ui.theme.ContactListTheme

class MainActivity : ComponentActivity() {
    var contactItems: List<String> by mutableStateOf(emptyList()) // Use List instead of MutableSet

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ContactListTheme {
                Scaffold {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = Screen.Home.route) {
                        composable(route = Screen.Home.route) {
                            PhoneScreen(navController)
                        }
                        composable(Screen.Detail.route) {
                            ContactList(contact = contactItems, navController)
                        }
                        composable(
                            Screen.contactDetail.route + "/{num}/{name}",
                            arguments = listOf(
                                /*navArgument("pic") {
                                    type = StringType
                                },
                    */
                                navArgument("num") {
                                    type = StringType
                                },

                                navArgument("name") {
                                    type = StringType
                                },
                            )
                        ) {
                            /*val pic = it.arguments?.getString("pic")*/
                            val num = it.arguments?.getString("num")
                            val name = it.arguments?.getString("name")
                            ContactDetail(navController = navController, num, name)
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
    private fun getContactList(): List<String> {
        val contactsList = mutableListOf<String>()
        val contentResolver = contentResolver
        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            null,
            null,
            null
        )
        cursor?.use {
            while (it.moveToNext()) {
                val name = it.getString(it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                val number =
                    it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                contactsList.add((name to number).toString())
            }
        }
        return contactsList
    }


    @OptIn(ExperimentalMaterial3Api::class)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @Composable
    fun PhoneScreen(navController: NavController) {
        var phone by remember {
            mutableStateOf(false)
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
                            modifier = Modifier.clickable { navController.navigate(Screen.Detail.route) })
                    }

                }


            }, colors = TopAppBarDefaults.topAppBarColors(Color.White), actions = {
                Icon(imageVector = Icons.Outlined.MoreVert, contentDescription = "")
            }, navigationIcon = {
                Text(text = "Edit", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
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
            var phonetext by remember {
                mutableStateOf("")
            }
            val bottom = rememberModalBottomSheetState()
            if (phone) {
                ModalBottomSheet(
                    onDismissRequest = { bottom },
                    modifier = Modifier.height(200.dp),
                    containerColor = Color.White
                ) {
                    Card(elevation = CardDefaults.cardElevation(10.dp)) {
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
                            keyboardActions = KeyboardActions(onDone = {}),
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Done,
                                keyboardType = KeyboardType.Number,
                                autoCorrect = true
                            ),
                        )
                    }

                }
            }
        }
    }


    @OptIn(ExperimentalMaterial3Api::class)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @Composable
    fun ContactList(contact: List<String>, navController: NavController) {
        var textField by remember {
            mutableStateOf("")
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
                            modifier = Modifier.clickable { navController.navigate(Screen.Home.route) })
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
                            focusedContainerColor = Color.LightGray,
                            unfocusedContainerColor = Color.LightGray,
                            focusedLeadingIconColor = Color.Black,
                            unfocusedLeadingIconColor = Color.Black,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .height(50.dp)
                            .width(300.dp),
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

                        )
                }


            }, colors = TopAppBarDefaults.topAppBarColors(Color.White), actions = {
                Icon(imageVector = Icons.Outlined.MoreVert, contentDescription = "")
            }, navigationIcon = {
                Text(text = "Edit", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            })
        }, floatingActionButton = {

            Card(
                modifier = Modifier.size(50.dp),
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
                    .padding(start = 50.dp, top = 100.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(7.dp),
                horizontalAlignment = Alignment.Start
            ) {
                items(contact) { contactInfo ->
                    val (name, number) = contactInfo.split(", ") // Splitting the contact info into name and number
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { navController.navigate(Screen.contactDetail.route + "/$number/$name") },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.profile),
                            contentDescription = "",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                        )

                        Column {
/*
                            Text(text = number)
*/
                            Text(text = name.replace("(", ""))
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
fun ContactDetail(navController: NavController, num: String?, name: String?) {
    Scaffold(topBar = {
        TopAppBar(title = { }, navigationIcon = {
            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "")
        }, colors = TopAppBarDefaults.topAppBarColors(Color.White), actions = {
            Icon(imageVector = Icons.Outlined.Star, contentDescription = "")
            Spacer(modifier = Modifier.width(20.dp))
            Icon(imageVector = Icons.Outlined.MoreVert, contentDescription = "")
        })
    }) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it.calculateTopPadding()),
            verticalArrangement = Arrangement.spacedBy(
                15.dp
            ), horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.profile), contentDescription = "",
                modifier = Modifier
                    .size(120.dp)
                    .clip(
                        CircleShape
                    ),
                contentScale = ContentScale.Crop,
            )

            if (num != null) {
                name?.replace("(", "")?.let { it1 ->
                    Text(
                        text = it1,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
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
                    Icon(imageVector = Icons.Outlined.Call, contentDescription = "")
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                Icon(imageVector = Icons.Outlined.Call, contentDescription = "")
                Column(
                    modifier = Modifier.wrapContentWidth(),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    num?.replace(")", "")?.let { it1 -> Text(text = it1, fontSize = 20.sp, fontWeight = FontWeight.W400)}
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                    Text(text = "Mobile | ", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color.LightGray)
                        Text(text = "Country", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color.LightGray)
                    }
                }
            }
        }
    }
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
    val route: String
) {
    object Home : Screen("home")
    object Detail : Screen("detail")
    object contactDetail : Screen("contactDetail")

}