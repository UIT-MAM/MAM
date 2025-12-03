package com.example.mam.screen.authentication

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.mam.component.CircleIconButton
import com.example.mam.component.OuterShadowFilledButton
import com.example.mam.ui.theme.OrangeDefault
import com.example.mam.ui.theme.WhiteDefault
import com.example.mam.utils.rememberQrBitmap
import com.example.mam.viewmodel.authentication.TwoFaViewModel // Import ViewModel

@Composable
fun SetupTwoFaScreen(
    viewModel: TwoFaViewModel,
    onBackClicked: () -> Unit,
    onNextClicked: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Lấy state từ ViewModel
    val state by viewModel.state.collectAsState()

    // Gọi API lấy QR Code ngay khi vào màn hình
    LaunchedEffect(Unit) {
        viewModel.loadSetupInfo()
    }

    Column(
        modifier = modifier // Sử dụng tham số modifier
            .fillMaxSize()
            .background(OrangeDefault)
    ) {
        // --- Header ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp, bottom = 20.dp, start = 16.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircleIconButton(
                icon = Icons.Default.ArrowBack,
                onClick = onBackClicked,
                backgroundColor = Color.White.copy(alpha = 0.2f),
                foregroundColor = WhiteDefault
            )
            Text(
                text = "Xác thực 2 yếu tố",
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
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "1. Cài đặt ứng dụng Google Authenticator.\n\n2. Quét mã QR này bằng ứng dụng:",
                        fontSize = 14.sp,
                        color = Color.DarkGray,
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    val qrCodeContent = state.qrCodeUrl // Lấy nội dung từ server
                    if (qrCodeContent.isNotEmpty()) {
                        // Tạo ảnh Bitmap từ chuỗi text
                        val qrBitmap = rememberQrBitmap(content = qrCodeContent)

                        if (qrBitmap != null) {
                            Image(
                                bitmap = qrBitmap, // Dùng bitmap thay vì painter
                                contentDescription = "QR Code",
                                modifier = Modifier
                                    .size(200.dp)
                                    .background(Color.White) // Đảm bảo nền trắng để dễ quét
                                    .padding(10.dp),         // Padding một chút
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            // Trường hợp tạo lỗi
                            Text("Không thể tạo mã QR", color = Color.Red)
                        }
                    } else {
                        // Trường hợp đang tải hoặc chuỗi rỗng
                        if (state.isLoading) {
                            CircularProgressIndicator()
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Không quét được?",
                        fontWeight = FontWeight.Bold,
                        color = OrangeDefault
                    )
                    Text(
                        text = "Hãy nhập thủ công khóa này:",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Text(
                        buildAnnotatedString {
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Color.Black)) {
                                append(state.secretKey)
                            }
                        },
                        fontSize = 16.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    OuterShadowFilledButton(
                        text = "Tiếp theo",
                        onClick = { onNextClicked(state.secretKey) },
                        modifier = Modifier.fillMaxWidth(0.6f)
                    )
                }
            }

            // Hiển thị lỗi nếu có
            if (state.error != null) {
                Text(
                    text = state.error ?: "",
                    color = Color.Red,
                    modifier = Modifier.align(Alignment.BottomCenter).padding(20.dp)
                )
            }
        }
    }
}