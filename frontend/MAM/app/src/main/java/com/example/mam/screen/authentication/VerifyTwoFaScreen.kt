package com.example.mam.screen.authentication

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mam.component.CircleIconButton
import com.example.mam.component.OuterShadowFilledButton
import com.example.mam.component.newOtpInputField
import com.example.mam.ui.theme.OrangeDefault
import com.example.mam.ui.theme.WhiteDefault
import com.example.mam.viewmodel.twofa.TwoFaViewModel
import kotlinx.coroutines.launch

@Composable
fun VerifyTwoFaScreen(
    viewModel: TwoFaViewModel,
    onBackClicked: () -> Unit,
    onSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // State nhập liệu OTP
    var otpValue by remember { mutableStateOf(List(6) { "" }) }
    val focusRequesters = remember { List(6) { FocusRequester() } }

    Column(
        modifier = modifier.fillMaxSize().background(OrangeDefault)
    ) {
        // --- Header ---
        Row(
            modifier = Modifier.padding(top = 40.dp, bottom = 20.dp, start = 16.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircleIconButton(
                icon = Icons.Default.ArrowBack,
                onClick = onBackClicked,
                backgroundColor = Color.White.copy(alpha = 0.2f),
                foregroundColor = WhiteDefault,
                image = TODO(),
                isEnable = TODO(),
                shadow = TODO(),
                isBadges = TODO(),
                badgesCount = TODO(),
                modifier = TODO()
            )
            Text(
                text = "Xác thực OTP",
                color = WhiteDefault,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.size(40.dp))
        }

        // --- Body ---
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                .background(WhiteDefault)
        ) {
            Column(
                modifier = Modifier.padding(24.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Nhập mã 6 số từ ứng dụng xác thực",
                    fontSize = 16.sp,
                    color = Color.DarkGray
                )

                Spacer(modifier = Modifier.height(24.dp))

                // OTP Inputs
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    otpValue.forEachIndexed { index, char ->
                        newOtpInputField(
                            char = char,
                            index = index,
                            focusedIndex = null,
                            focusRequester = focusRequesters[index],
                            onFocusChanged = { },
                            onCharacterChanged = { newChar ->
                                val newList = otpValue.toMutableList()
                                newChar?.length?.let {
                                    if (it <= 1) {
                                        newList[index] = newChar
                                        otpValue = newList
                                        if (newChar.isNotEmpty() && index < 5) {
                                            focusRequesters[index + 1].requestFocus()
                                        }
                                    }
                                }
                            },
                            onKeyboardBack = { },
                            modifier = Modifier.width(45.dp).aspectRatio(1f),
                            resetTrigger = false
                        )
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))

                if (state.isLoading) {
                    CircularProgressIndicator()
                } else {
                    OuterShadowFilledButton(
                        text = "Xác nhận",
                        onClick = {
                            val code = otpValue.joinToString("")
                            if (code.length == 6) {
                                scope.launch {
                                    val result = viewModel.verifyOtp(code)
                                    if (result == 1) {
                                        Toast.makeText(context, "Thành công!", Toast.LENGTH_SHORT).show()
                                        onSuccess()
                                    } else {
                                        Toast.makeText(context, "Mã không đúng", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else {
                                Toast.makeText(context, "Vui lòng nhập đủ 6 số", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(0.6f)
                    )
                }
            }
        }
    }
}