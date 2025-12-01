package com.example.mam.screen.authentication

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mam.component.CircleIconButton
import com.example.mam.component.OtpInputWithCountdown
import com.example.mam.component.OuterShadowFilledButton
import com.example.mam.component.newOtpInputField
import com.example.mam.component.outerShadow
import com.example.mam.ui.theme.BrownDefault
import com.example.mam.ui.theme.GreyDark
import com.example.mam.ui.theme.OrangeDefault
import com.example.mam.ui.theme.OrangeLight
import com.example.mam.ui.theme.OrangeLighter
import com.example.mam.ui.theme.Variables
import com.example.mam.ui.theme.WhiteDefault
import com.example.mam.viewmodel.authentication.otp.OtpAction
import com.example.mam.viewmodel.authentication.otp.OtpState
import com.plcoding.composeotpinput.OtpViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun OTPScreen(
    focusRequester: List<FocusRequester> = List(6) { FocusRequester() },
    viewModel: OtpViewModel = viewModel(),
    onAction: (OtpAction) -> Unit = viewModel::onAction,
    onVerifyClicked: () -> Unit = {},
    onCloseClicked: () -> Unit = {},
    resetTrigger: Boolean = false,
    modifier: Modifier = Modifier
) {
    val uiSate by viewModel.state.collectAsState()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val email = viewModel.email.collectAsStateWithLifecycle().value

    OTPScreenContent(
        viewModel = viewModel,
        state = state,
        email = email,
        focusRequester = focusRequester,
        onAction = onAction,
        onVerifyClicked = onVerifyClicked,
        onCloseClicked = onCloseClicked,
        resetTrigger = resetTrigger,
        modifier = modifier
    )
}

@Composable
fun OTPScreenContent(
    viewModel: OtpViewModel = viewModel(),
    state: OtpState,
    email: String,
    focusRequester: List<FocusRequester>,
    onAction: (OtpAction) -> Unit,
    onVerifyClicked: () -> Unit,
    onCloseClicked: () -> Unit,
    resetTrigger: Boolean,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Quản lý focus và bàn phím
    val focusManager = LocalFocusManager.current
    val keyboardManager = LocalSoftwareKeyboardController.current


    // Tự động focus vào ô tương ứng
    LaunchedEffect(state.focusedIndex) {
        state.focusedIndex?.let { index ->
            focusRequester.getOrNull(index)?.requestFocus()
        }
    }

    LaunchedEffect(state.code, keyboardManager) {
        val allNumbersEntered = state.code.none { it == null }
        if(allNumbersEntered) {
            focusRequester.forEach {
                it.freeFocus()
            }
            focusManager.clearFocus()
            keyboardManager?.hide()
        }
    }

    var resetTrigger by remember { mutableStateOf(false) } // State để trigger reset

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(WhiteDefault)
    ) {
        val boxScope = this
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.9f)
                .wrapContentHeight()
                .background(
                    color = OrangeDefault,
                    shape = RoundedCornerShape(
                        size = 50.dp
                    )
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp)
            ) {
                // Icon nằm trái
                CircleIconButton(
                    backgroundColor = OrangeLighter,
                    foregroundColor = OrangeDefault,
                    icon = Icons.Filled.Close,
                    shadow = "outer",
                    onClick = onCloseClicked,
                    modifier = Modifier
                        .focusable(false)
                        .align(Alignment.CenterStart)
                        .padding(start = 16.dp) // padding nếu cần
                )
                // Text nằm giữa
                Text(
                    text = "Quên mật khẩu",
                    style = TextStyle(
                        fontSize = Variables.HeadlineMediumSize,
                        lineHeight = Variables.HeadlineMediumLineHeight,
                        fontWeight = FontWeight(700),
                        color = WhiteDefault,
                        textAlign = TextAlign.Center,
                    ),
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(18.dp, Alignment.Top),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusable(false)
                    .outerShadow(
                        color = GreyDark,
                        bordersRadius = 50.dp,
                        offsetX = 0.dp,
                        offsetY = -4.dp,
                    )
                    .wrapContentHeight()
                    .background(
                        color = OrangeLighter,
                        shape = RoundedCornerShape(
                            size = 50.dp
                        )
                    )
            ) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Chúng tôi sẽ gửi mã OTP đến \nemail của bạn",
                    style = TextStyle(
                        fontSize = Variables.BodySizeMedium,
                        lineHeight = 22.4.sp,
                        fontWeight = FontWeight(Variables.BodyFontWeightRegular),
                        color = BrownDefault,
                        textAlign = TextAlign.Center,
                    )
                )
                Text(
                    text = email,
                    style = TextStyle(
                        fontSize = Variables.BodySizeMedium,
                        lineHeight = 22.4.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrownDefault,
                        textAlign = TextAlign.Center,
                    )
                )
                Box(
                    modifier = Modifier
                        .width(330.dp)
                        .height(49.dp)
                        .background(
                            color = OrangeLight,
                            shape = RoundedCornerShape(40.dp)
                        )
                        .padding(start = 14.dp, end = 14.dp)
                        .focusable(false)
                ) {
//                    OtpInputField(
//                        otpLength = 4,
//                        onOtpChange = { viewModel.setOTP((it))
//                        },
//                        onOtpComplete = { viewModel.setOTP((it))
//                        },
//                        resetTrigger = resetTrigger
//                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(
                            5.dp,
                            Alignment.CenterHorizontally
                        ),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(vertical = 16.dp)
                            .fillMaxWidth()
                            .wrapContentHeight()
                    ) {
                        state.code.forEachIndexed { index, number ->
                            newOtpInputField(
                                char = number,
                                index = index,
                                focusedIndex = state.focusedIndex,
                                focusRequester = focusRequester[index],
                                onFocusChanged = { isFocused ->
                                    if (isFocused && state.focusedIndex != index) {
                                        onAction(OtpAction.OnChangeFieldFocused(index))
                                    }
                                },
                                onCharacterChanged = { newChar ->
                                    onAction(OtpAction.OnEnterCharacter(newChar, index))
                                },
                                onKeyboardBack = {
                                    onAction(OtpAction.OnKeyboardBack)
                                },
                                modifier = Modifier
                                    .width(42.dp)
                                    .aspectRatio(1f),
                                resetTrigger = resetTrigger
                            )
                        }
                    }
                }
                OtpInputWithCountdown(
                    onResendClick = {
                        scope.launch {
                            if (viewModel.reSendOTP() == 1) {
                                Toast.makeText(
                                    context,
                                    "Mã OTP đã được gửi lại đến email của bạn",
                                    Toast.LENGTH_SHORT
                                ).show()
                                resetTrigger = !resetTrigger
                                viewModel.setOTP((""))
                            } else {
                                Toast.makeText(
                                    context,
                                    "Gửi lại mã OTP thất bại, vui lòng thử lại sau",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                )
                OuterShadowFilledButton(
                    text = "Xác nhận",
                    onClick = {
                        viewModel.setOTP(state.code.joinToString(""))
                        scope.launch {
                            if (viewModel.verifyOtp() == 1) {
                                Toast.makeText(
                                    context,
                                    "Đổi mật khẩu mới thành công",
                                    Toast.LENGTH_SHORT
                                ).show()
                                onVerifyClicked()
                            } else {
                                Toast.makeText(
                                    context,
                                    "Xác nhận OTP thất bại, vui lòng thử lại sau",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    },
                    //isEnable = viewModel.isOTPValid(),
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(40.dp),
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

class FakeOtpHandler {
    private val _state = MutableStateFlow(OtpState())
    val state: StateFlow<OtpState> = _state

    val email = MutableStateFlow("preview@email.com")

    fun onAction(action: OtpAction) {
        when(action) {
            is OtpAction.OnChangeFieldFocused -> {
                _state.update { it.copy(
                    focusedIndex = action.index
                ) }
            }
            is OtpAction.OnEnterCharacter -> {
                enterNumber(action.number, action.index)
            }
            OtpAction.OnKeyboardBack -> {
                val currentIndex = state.value.focusedIndex ?: return
                val currentChar = state.value.code.getOrNull(currentIndex)

                if (currentChar != null) {
                    // Xóa ký tự tại ô hiện tại
                    _state.update {
                        it.copy(
                            code = it.code.mapIndexed { index, number ->
                                if (index == currentIndex) null else number
                            },
                            focusedIndex = currentIndex
                        )
                    }
                } else {
                    // Lùi về ô trước
                    val previousIndex = getPreviousFocusedIndex(currentIndex)
                    _state.update {
                        it.copy(
                            code = it.code.mapIndexed { index, number ->
                                if (index == previousIndex) null else number
                            },
                            focusedIndex = previousIndex
                        )
                    }
                }
            }
        }
    }
    private fun enterNumber(number: String?, index: Int) {
        val currentCode = state.value.code
        val newCode = currentCode.mapIndexed { i, c ->
            if (i == index) number else c
        }

        val wasNumberRemoved = number == null

        val nextIndex = if (wasNumberRemoved) {
            index
        } else {
            // Tìm ô đầu tiên chưa được nhập lại (null) sau index hiện tại
            (index + 1..5).firstOrNull { newCode[it] == null } ?: index
        }

        _state.update {
            it.copy(
                code = newCode,
                focusedIndex = nextIndex
            )
        }
    }

    private fun getPreviousFocusedIndex(currentIndex: Int?): Int? {
        return currentIndex?.minus(1)?.coerceAtLeast(0)
    }

    private fun getNextFocusedTextFieldIndex(
        currentCode: List<String?>,
        currentFocusedIndex: Int?
    ): Int? {
        if(currentFocusedIndex == null) {
            return null
        }

        if(currentFocusedIndex == 5) {
            return currentFocusedIndex
        }

        return getFirstEmptyFieldIndexAfterFocusedIndex(
            code = currentCode,
            currentFocusedIndex = currentFocusedIndex
        )
    }

    private fun getFirstEmptyFieldIndexAfterFocusedIndex(
        code: List<String?>,
        currentFocusedIndex: Int
    ): Int {
        for (index in (currentFocusedIndex + 1) until code.size) {
            if (code[index] == null) {
                return index
            }
        }
        return currentFocusedIndex
    }
}

//@Preview(showBackground = true)
//@Composable
//fun PreviewOTPScreen() {
//    val handler = remember { FakeOtpHandler() }
//
//    val state by handler.state.collectAsState()
//    val email by handler.email.collectAsState()
//
//    val focusRequesters = remember { List(6) { FocusRequester() } }
//
//    OTPScreenContent(
//        state = state,
//        email = email,
//        focusRequester = focusRequesters,
//        onAction = handler::onAction,
//        onVerifyClicked = {},
//        onCloseClicked = {},
//        resetTrigger = false
//    )
//}


//@Preview(showBackground = true)
//@Composable
//fun PreviewForgetPasswordScreen() {
//    OTPScreen()
//}
