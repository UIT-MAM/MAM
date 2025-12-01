package com.example.mam.screen.authentication

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mam.component.CircleIconButton
import com.example.mam.component.OuterShadowFilledButton
import com.example.mam.ui.theme.OrangeDefault
import com.example.mam.ui.theme.WhiteDefault
import com.example.mam.viewmodel.twofa.TwoFaViewModel

@Composable
fun RecoveryCodeScreen(
    viewModel: TwoFaViewModel,
    onDoneClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Lấy danh sách mã từ ViewModel
    val state by viewModel.state.collectAsState()
    val codes = state.recoveryCodes

    Column(
        modifier = modifier.fillMaxSize().background(OrangeDefault)
    ) {
        // --- Header ---
        Row(
            modifier = Modifier.padding(top = 40.dp, bottom = 20.dp, start = 16.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircleIconButton(
                icon = Icons.Default.Close,
                onClick = onDoneClicked,
                backgroundColor = Color.White.copy(alpha = 0.2f),
                foregroundColor = WhiteDefault
            )
            Text(
                text = "Mã khôi phục",
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
                Text(
                    text = "Hãy lưu lại các mã này. Bạn có thể dùng chúng để đăng nhập nếu mất điện thoại.",
                    textAlign = TextAlign.Center,
                    color = Color(0xFF8D6E63),
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFF8E1), RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0xFFD7CCC8), RoundedCornerShape(8.dp))
                        .padding(20.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        if (codes.isEmpty()) {
                            Text("Đang tải mã...", color = Color.Gray)
                        } else {
                            // Hiển thị mã, 2 mã mỗi dòng hoặc list dọc
                            codes.forEach { code ->
                                Text(
                                    text = code,
                                    color = Color(0xFF5D4037),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                OuterShadowFilledButton(
                    text = "Hoàn thành",
                    onClick = onDoneClicked,
                    modifier = Modifier.fillMaxWidth(0.6f)
                )
            }
        }
    }
}