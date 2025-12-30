package com.example.mam.screen.authentication

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mam.GoogleSignInUtils
import com.example.mam.R
import com.example.mam.component.CircleIconButton
import com.example.mam.component.CustomDialog
import com.example.mam.component.EditField
import com.example.mam.component.LoadingAlertDialog
import com.example.mam.component.OuterShadowFilledButton
import com.example.mam.component.PasswordField
import com.example.mam.component.UnderlinedClickableText
import com.example.mam.component.outerShadow
import com.example.mam.ui.theme.BrownDefault
import com.example.mam.ui.theme.GreyDark
import com.example.mam.ui.theme.OrangeDefault
import com.example.mam.ui.theme.OrangeLighter
import com.example.mam.ui.theme.Typography
import com.example.mam.ui.theme.WhiteDefault
import com.example.mam.viewmodel.authentication.SignInViewModel
import kotlinx.coroutines.launch

@Composable
fun SignInScreen(
    onSignInClicked: () -> Unit = {},
    onSignInManager: () -> Unit = {},
    onForgotClicked: () -> Unit = {},
    onBackClicked: () -> Unit ={},
    viewModel: SignInViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val signInState = viewModel.signInState.collectAsStateWithLifecycle().value
    val isLoading = viewModel.isLoading.collectAsStateWithLifecycle().value
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) {
        GoogleSignInUtils.getGoogleIdToken(
            context = context,
            scope = scope,
            launcher = null,
            handle = { token ->

            }
        )

    }

    var isShowDeletedDialog by remember { mutableStateOf(false)}
    var isShowBlockedDialog by remember { mutableStateOf(false)}
    var isShowPendingDialog by remember { mutableStateOf(false)}
    if (isLoading) {
        LoadingAlertDialog()
    }
    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .background(color = OrangeDefault)
            .padding(WindowInsets.statusBars.asPaddingValues())
            //.padding(WindowInsets.ime.asPaddingValues())
            .verticalScroll(scrollState),
    ) {
        if (isShowDeletedDialog) {
            CustomDialog(
                title = "Thông báo",
                message = "Tài khoản của bạn đã bị xóa. Vui lòng liên hệ với quản trị viên để biết thêm chi tiết.",
                onDismiss = { isShowDeletedDialog = false },
                onConfirm = { isShowDeletedDialog = false },
                isHavingCancelButton = false
            )
        }
        if (isShowBlockedDialog) {
            CustomDialog(
                title = "Thông báo",
                message = "Tài khoản của bạn đã bị khóa. Vui lòng liên hệ với quản trị viên để biết thêm chi tiết.",
                onDismiss = { isShowBlockedDialog = false },
                onConfirm = { isShowBlockedDialog = false },
                isHavingCancelButton = false
            )
        }
        if (isShowPendingDialog) {
            CustomDialog(
                title = "Thông báo",
                message = "Tài khoản của bạn chưa được kích hoạt.\nVui lòng kiểm tra email để kích hoạt tài khoản\nhoặc\nXác nhận gửi lại email kích hoạt.",
                onDismiss = { isShowPendingDialog = false },
                onConfirm = {
                    scope.launch {
                        viewModel.resendVerificationEmail()
                        isShowPendingDialog = false
                    }
                },
                isHavingCancelButton = true
            )
        }
        Box(
            contentAlignment = Alignment.TopStart
        ) {
            CircleIconButton(
                backgroundColor = OrangeLighter,
                foregroundColor = OrangeDefault,
                icon = Icons.Filled.ArrowBack,
                shadow = "outer",
                onClick = onBackClicked,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 16.dp, top = 16.dp)
            )
            Image(
                painter = painterResource(id = R.drawable.ic_mam_foreground),
                contentDescription = null,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier.fillMaxWidth()
            )
        }
        Text(
            text = "Đăng nhập",
            style = Typography.titleLarge
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .outerShadow(
                    color = GreyDark,
                    bordersRadius = 50.dp,
                    blurRadius = 4.dp,
                    offsetX = 0.dp,
                    offsetY = -4.dp,
                )
                .fillMaxWidth()
                .wrapContentHeight()
                .background(
                    color = OrangeLighter,
                    shape = RoundedCornerShape(
                        topStart = 50.dp,
                        topEnd = 50.dp,
                        bottomStart = 0.dp,
                        bottomEnd = 0.dp
                    )
                )
        ) {
            Spacer(Modifier.height(10.dp))
            Column(
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalAlignment = Alignment.Start,
                modifier = Modifier
                    .fillMaxWidth(0.8f)
            ) {
                EditField(
                    label = "Số điện thoại/Email/Username",
                    value = signInState.credentialId,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    onValueChange = { viewModel.setUsername(it) },
                    modifier = Modifier.fillMaxWidth()
                )
                PasswordField(
                    label = "Mật khẩu",
                    value = signInState.password,
                    onValueChange = { viewModel.setSIPassword(it)},
                    modifier = Modifier.fillMaxWidth()
                )
                UnderlinedClickableText(
                    link = "Quên mật khẩu",
                    onClick = onForgotClicked,
                    modifier = Modifier
                        .align(Alignment.Start)
                    )
            }

            OuterShadowFilledButton(
                text = "Đăng nhập",
                onClick = {
                    scope.launch {
                        viewModel.triggerLoading()
                        val message = viewModel.SignIn()
                        if (message.first == "USER" && message.second == "ACTIVE") {
                            Toast.makeText(
                            context,
                            "Đăng nhập thành công",
                            Toast.LENGTH_SHORT
                        ).show()
                            onSignInClicked()

                        } else if (message.first == "ADMIN" && message.second == "ACTIVE") {
                            Toast.makeText(
                            context,
                            "Đăng nhập thành công",
                            Toast.LENGTH_SHORT
                        ).show()
                            onSignInManager()
                        } else if (message.second == "DELETED") {
                            Toast.makeText(
                                context,
                                "Tài khoản của bạn đã bị xóa",
                                Toast.LENGTH_SHORT
                            ).show()
                            isShowDeletedDialog = true
                        } else if (message.second == "BLOCKED") {
                            Toast.makeText(
                                context,
                                "Tài khoản của bạn đã bị khóa",
                                Toast.LENGTH_SHORT
                            ).show()
                            isShowBlockedDialog = true
                        } else if (message.second == "PENDING") {
                            isShowPendingDialog = true
                        }
                        else {
                            Toast.makeText(
                                context,
                                message.second,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                },
                isEnable = (signInState.password.isNotEmpty() && signInState.credentialId.isNotEmpty()),
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(40.dp),
            )

//            OuterShadowFilledButton(
//                text = "Đăng nhập với Google",
//                onClick = {
//                    viewModel.triggerLoading()
//                   GoogleSignInUtils.getGoogleIdToken(
//                        context = context,
//                        scope = scope,
//                        launcher = launcher,
//                        timeout = {
//                            viewModel.resetLoading()
//                        },
//                        handle = { idToken ->
//                            scope.launch {
//                                val message = viewModel.signInWithFirebase(idToken)
//                                if (message.first == "USER" && message.second == "ACTIVE") {
//                                    Toast.makeText(
//                                        context,
//                                        "Đăng nhập thành công",
//                                        Toast.LENGTH_SHORT
//                                    ).show()
//                                    onSignInClicked()
//
//                                } else if (message.first == "ADMIN" && message.second == "ACTIVE") {
//                                    Toast.makeText(
//                                        context,
//                                        "Đăng nhập thành công",
//                                        Toast.LENGTH_SHORT
//                                    ).show()
//                                    onSignInManager()
//                                } else if (message.second == "DELETED") {
//                                    Toast.makeText(
//                                        context,
//                                        "Tài khoản của bạn đã bị xóa",
//                                        Toast.LENGTH_SHORT
//                                    ).show()
//                                    isShowDeletedDialog = true
//                                } else if (message.second == "BLOCKED") {
//                                    Toast.makeText(
//                                        context,
//                                        "Tài khoản của bạn đã bị khóa",
//                                        Toast.LENGTH_SHORT
//                                    ).show()
//                                    isShowBlockedDialog = true
//                                } else if (message.second == "PENDING") {
//                                    isShowPendingDialog = true
//                                }
//                                else {
//                                    Toast.makeText(
//                                        context,
//                                        message.second,
//                                        Toast.LENGTH_SHORT
//                                    ).show()
//                                }
//                            }
//                        }
//
//                    )
//                },
//                color = WhiteDefault,
//                textColor = BrownDefault,
//                image = R.drawable.ic_google,
//                modifier = Modifier
//                    .fillMaxWidth(0.7f)
//                    .height(40.dp),
//            )
            Spacer(Modifier.height(10.dp))
        }
    }
}


@SuppressLint("SuspiciousIndentation")
@Preview(showBackground = true)
@Composable
fun SignInScreenPreview() {
        SignInScreen()
}