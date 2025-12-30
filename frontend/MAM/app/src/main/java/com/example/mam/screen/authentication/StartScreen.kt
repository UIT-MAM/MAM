package com.example.mam.screen.authentication

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mam.R
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mam.GoogleSignInUtils
import com.example.mam.component.LoadingAlertDialog
import com.example.mam.component.OuterShadowFilledButton
import com.example.mam.component.SignUpDialog
import com.example.mam.component.UnderlinedClickableText
import com.example.mam.component.UnderlinedClickableText2
import com.example.mam.component.outerShadow
import com.example.mam.ui.theme.BrownDefault
import com.example.mam.ui.theme.GreyDark
import com.example.mam.ui.theme.OrangeDefault
import com.example.mam.ui.theme.OrangeLighter
import com.example.mam.ui.theme.Typography
import com.example.mam.ui.theme.WhiteDefault
import com.example.mam.viewmodel.authentication.StartViewModel
import kotlinx.coroutines.launch

@Composable
fun StartScreen(
    viewModel: StartViewModel = viewModel(),
    onSignInClicked: () -> Unit = {},
    onAutoSignIn: () -> Unit = {},
    onSignInManager: () -> Unit = {},
    onSignUpClicked: () -> Unit = {},
    onGGSignUpClicked: () -> Unit = {},
    onTermsClicked: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val isLoading = viewModel.isLoading.collectAsStateWithLifecycle().value
    var isGoogleRegister by remember { mutableStateOf(false) }

    var phoneNumber by remember { mutableStateOf("") }
    var idToken by remember { mutableStateOf("") }

    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) {
        GoogleSignInUtils.getGoogleIdToken(
            context = context,
            scope = scope,
            launcher = null,
            handle = { token ->
                idToken = token
            }
        )

    }

    if (isLoading) {
        isGoogleRegister = false
        LoadingAlertDialog()
    }
    LaunchedEffect(Unit) {
        scope.launch {
            if (viewModel.getAccessToken() == 1) {
                onSignInManager()
                Toast.makeText(
                    context,
                    "Đăng nhập thành công",
                    Toast.LENGTH_SHORT
                ).show()
            }
            else if (viewModel.getAccessToken() == 2) {
                onAutoSignIn()
                Toast.makeText(
                    context,
                    "Đăng nhập thành công",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .background(color = OrangeDefault)
            .padding(WindowInsets.statusBars.asPaddingValues())
            .verticalScroll(scrollState)
            //.padding(WindowInsets.ime.asPaddingValues())
    ) {

//        if (isGoogleRegister) {
//            SignUpDialog(
//                title = "Đăng ký qua Google",
//                message = "Nhập số điện thoại của bạn để đăng ký tài khoản",
//                viewModel = viewModel,
//                onDismiss = { isGoogleRegister = false },
//                onConfirm = {
//                    viewModel.triggerLoading()
//                    GoogleSignInUtils.getGoogleIdToken(
//                        context = context,
//                        scope = scope,
//                        launcher = launcher,
//                        timeout = {
//                            isGoogleRegister = false
//                            viewModel.resetLoading()
//                        },
//                        handle = { token ->
//                            idToken = token
//                            scope.launch {
//                                val result = viewModel.RegisterWithFireBase(idToken)
//                                if (result == 1) {
//                                    onGGSignUpClicked()
//                                    Toast.makeText(
//                                        context,
//                                        "Đăng ký thành công",
//                                        Toast.LENGTH_SHORT
//                                    ).show()
//                                    onSignInClicked()
//                                    isGoogleRegister = false
//                                } else {
//                                    Toast.makeText(
//                                        context,
//                                        "Đăng ký thất bại",
//                                        Toast.LENGTH_SHORT
//                                    ).show()
//                                }
//                            }
//                        }
//                    )
//                },
//            )
//        }
        Image(
            painter = painterResource(id = R.drawable.ic_mam_foreground),
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = "MaM chào bạn!",
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
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth(0.8f)
            ) {
//                OuterShadowFilledButton(
//                    text = "Đăng ký qua Google",
//                    image = R.drawable.ic_google,
//                    color = WhiteDefault,
//                    textColor = BrownDefault,
//                    onClick = {
//                        isGoogleRegister =true
//                    },
//                    modifier = Modifier
//                        .fillMaxWidth(0.8f)
//                )
//                HorizontalDivider(
//                    color = BrownDefault,
//                    modifier = Modifier
//                        .fillMaxWidth(0.8f),
//                )
                OuterShadowFilledButton(
                    text = "Đăng ký",
                    onClick = onSignUpClicked,
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                )
//                UnderlinedClickableText(
//                    text = "Bạn đã có tài khoản? ",
//                    link = "Đăng nhập ngay",
//                    linkColor = OrangeDefault,
//                    onClick = onSignInClicked,
//                    modifier = Modifier.padding(0.dp)
//                )
                UnderlinedClickableText2(
                    text = "Bạn đã có tài khoản? ",
                    link = "Đăng nhập ngay",
                    linkColor = OrangeDefault,
                    onClick = onSignInClicked,
                )
            }
            UnderlinedClickableText(
                text = "Nếu tiếp tục, bạn đã đồng ý với\n",
                link = "Điều khoản và Chính sách",
                onClick =
                {
                    Toast.makeText(
                        context,
                        "Điều khoản và Chính sách",
                        Toast.LENGTH_SHORT
                    ).show()
                },
                modifier = Modifier
            )
            Spacer(Modifier.height(10.dp))
        }
    }
}


@Preview(showBackground = true)
@Composable
fun StartScreenPreview() {
    val viewModel = StartViewModel.Factory.create(StartViewModel::class.java)
    StartScreen(
    )
}