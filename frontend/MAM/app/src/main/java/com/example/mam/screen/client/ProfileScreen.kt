package com.example.mam.screen.client

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.ArrowForwardIos
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.mam.data.Constant.BASE_AVT
import com.example.mam.component.CircleIconButton
import com.example.mam.component.NormalButtonWithIcon
import com.example.mam.component.OuterShadowFilledButton
import com.example.mam.component.ProfileInput
import com.example.mam.component.UnderlinedClickableText
import com.example.mam.component.innerShadow
import com.example.mam.component.outerShadow
import com.example.mam.ui.theme.BrownDefault
import com.example.mam.ui.theme.GreyAvaDefault
import com.example.mam.ui.theme.GreyDark
import com.example.mam.ui.theme.OrangeDefault
import com.example.mam.ui.theme.OrangeLight
import com.example.mam.ui.theme.OrangeLighter
import com.example.mam.ui.theme.Typography
import com.example.mam.ui.theme.WhiteDefault
import com.example.mam.viewmodel.client.ProfileViewModel
import kotlinx.coroutines.launch

@SuppressLint("ContextCastToActivity")
@Composable
fun ProfileScreen(
    onChangePasswordClicked: () -> Unit = {},
    onLogoutClicked: () -> Unit = {},
    onBackClicked: () -> Unit = {},
    onHistoryClicked: () -> Unit = {},
    onTwoFaClicked: () -> Unit,
    isAdmin: Boolean = false,
    viewModel: ProfileViewModel
    ) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isPreview = LocalInspectionMode.current
    val activity = context as? Activity
    val user = viewModel.user.collectAsStateWithLifecycle().value
    val isEditingState = viewModel.isEditing.collectAsStateWithLifecycle()
    val isEditing = isEditingState.value
    val imagePicker = if (!isPreview) {
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                viewModel.setUserAvatar(uri.toString())
                viewModel.setUserAvatarFile(context,uri)
            }
        }
    } else null

    LaunchedEffect(key1 = isEditing) {
        viewModel.fetchUser()
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .wrapContentHeight()
            .background(color = OrangeDefault)
            .padding(WindowInsets.statusBars.asPaddingValues())
    ) {
        Box(
            contentAlignment = Alignment.CenterStart,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            CircleIconButton(
                backgroundColor = OrangeLighter,
                foregroundColor = OrangeDefault,
                icon = Icons.Filled.ArrowBack,
                shadow = "outer",
                onClick = onBackClicked,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 16.dp, start = 16.dp)
            )
            Text(
                text = "Trang cá nhân",
                style = Typography.titleLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 17.dp)
            )
        }
        Box(
            modifier = Modifier
                .padding(top = 15.dp)
                .fillMaxSize()
                .outerShadow(
                    color = GreyDark,
                    bordersRadius = 50.dp,
                    blurRadius = 4.dp,
                    offsetX = 0.dp,
                    offsetY = -4.dp,
                )
                .clip(RoundedCornerShape(topStart = 50.dp, topEnd = 50.dp))
                .background(OrangeLighter)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .align(Alignment.CenterHorizontally)
                ) {
                    AsyncImage(
                        model = user.avatarUrl?.let{
                            if(!isEditing)user.getRealURL()
                            else it
                        } ?: BASE_AVT,
                        contentDescription = "Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(180.dp)
                            .clip(CircleShape)
                            .background(GreyAvaDefault)
                    )
                    IconButton(
                        onClick = {
                            val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                Manifest.permission.READ_MEDIA_IMAGES
                            } else {
                                Manifest.permission.READ_EXTERNAL_STORAGE
                            }
                            if (context.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED && activity != null) {
                                ActivityCompat.requestPermissions(activity, arrayOf(permission), 123)
                            } else {
                                imagePicker?.launch("image/*")
                            }
                        },
                        enabled = isEditing,
                        modifier = Modifier
                            .size(30.dp)
                            .align(Alignment.BottomEnd)
                            .innerShadow(
                                color = GreyDark,
                                bordersRadius = 25.dp,
                                blurRadius = 10.dp,
                                offsetX = 0.dp,
                                offsetY = 10.dp
                            )
                            .background(OrangeDefault, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = "Settings",
                            tint = WhiteDefault,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
                ProfileInput(
                    label = "Tên người dùng",
                    value = user.username,
                    backgroundColor = WhiteDefault,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done
                    ),
                    onValueChange = {
                        viewModel.setUserName(it)
                    },
                    enabled = isEditing,
                    modifier = Modifier.fillMaxWidth()
                )
                ProfileInput(
                    label = "Họ tên",
                    value = user.fullname,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done
                    ),
                    onValueChange = {viewModel.setFullName(it)
                    },
                    enabled = isEditing,
                    modifier = Modifier.fillMaxWidth()
                )
                ProfileInput(
                    label = "Số điện thoại",
                    value = user.phone,
                    backgroundColor = WhiteDefault,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done
                    ),
                    onValueChange = {
                            viewModel.setPhoneNumber(it)
                    },
                    enabled = isEditing,
                    modifier = Modifier.fillMaxWidth()
                )
                ProfileInput(
                    label = "Email",
                    value = user.email,
                    backgroundColor = WhiteDefault,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done
                    ),
                    onValueChange = {
                    },
                    enabled = false,
                    modifier = Modifier.fillMaxWidth()
                )

                if (!isAdmin)
                NormalButtonWithIcon(
                    text = "Lịch sử đơn hàng",
                    onClick = onHistoryClicked,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(39.dp),
                    icon = Icons.Outlined.ArrowForwardIos,
                    color = OrangeLight,
                    textColor = BrownDefault,
                    tintIcon = BrownDefault,

                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),) 
                {
                    OuterShadowFilledButton(
                        text = "Đổi mật khẩu",
                        onClick = onChangePasswordClicked,
                        modifier = Modifier
                            .wrapContentWidth()
                            .height(40.dp)
                    )
                    OuterShadowFilledButton(
                        text = "Xác thực 2 yếu tố (2FA)",
                        onClick = onTwoFaClicked,
                        modifier = Modifier
                            .wrapContentWidth()
                            .height(40.dp)
                    )
                    OuterShadowFilledButton(
                        onClick = {
                            if(isEditing){
                                scope.launch {
                                    val result = viewModel.updateUser()
                                    Toast.makeText(
                                        context,
                                        when(result){
                                            1 -> "Chỉnh sửa thành công"
                                            else -> "Chỉnh sửa thất bại"
                                        },
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                            viewModel.setEditing(!isEditing)
                        },
                        text = if (isEditing) "Lưu" else "Chỉnh sửa",
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                    )
                }
                OuterShadowFilledButton(
                    text = "Đăng xuất",
                    onClick = {
                        scope.launch {
                            val result = viewModel.logOut()
                            if (result == 1) {
                                Toast.makeText(
                                    context,
                                    "Đăng xuất thành công",
                                    Toast.LENGTH_SHORT
                                ).show()
                                onLogoutClicked()
                            }
                            else Toast.makeText(
                                context,
                                "Đăng xuất thất bại",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .width(182.dp)
                        .height(40.dp)
                )
                UnderlinedClickableText(
                    link = "Chính sách và điều khoản",
                    onClick = {
                        Toast.makeText(
                            context,
                            "Điều khoản và chính sách",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally),
                    color = BrownDefault
                )
                Spacer(Modifier.height(10.dp))
            }
        }
    }
}
