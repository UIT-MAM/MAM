package com.example.mam.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mam.ui.theme.BrownDark
import com.example.mam.ui.theme.BrownDefault
import com.example.mam.ui.theme.BrownLight
import com.example.mam.ui.theme.GreyDark
import com.example.mam.ui.theme.GreyLight
import com.example.mam.ui.theme.OrangeDefault
import com.example.mam.ui.theme.Transparent
import com.example.mam.ui.theme.WhiteDefault
import kotlinx.coroutines.launch
import coil.compose.AsyncImage
import com.example.mam.R
import com.example.mam.dto.product.ProductResponse
import com.example.mam.dto.variation.VariationOptionRequest
import com.example.mam.dto.variation.VariationOptionResponse

@Composable
fun UnderlinedClickableText(
    text: String = "",
    link: String,
    color: Color = BrownDefault,
    linkColor: Color = OrangeDefault,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val annotatedText = buildAnnotatedString {
        withStyle(style = SpanStyle(color = color)) {
            append(text)
        }
        append("   ")
        // Đánh dấu phần cần click
        pushStringAnnotation(tag = "CLICK", annotation = "clicked")
        withStyle(style = SpanStyle(
            color = linkColor,
            textDecoration = TextDecoration.Underline,
            fontWeight = FontWeight.Bold
        )) {
            append(link)
        }
        pop()
    }


    ClickableText(
        text = annotatedText,
        style = TextStyle(
            textAlign = TextAlign.Center),
        onClick = { offset ->
            annotatedText.getStringAnnotations(tag = "CLICK", start = offset, end = offset)
                .firstOrNull()?.let {
                    onClick()
                }
        },
        modifier = modifier

        // style mặc định cho đoạn text
    )
}

@Composable
fun InnerShadowFilledButton(
    text: String,
    icon: ImageVector?= null,
    image: Int ?= null,
    isEnable: Boolean = true,
    onClick: () -> Unit,
    modifier: Modifier
){
    val isPressed = remember { mutableStateOf(false) }
    Button(
        colors = ButtonDefaults.buttonColors(containerColor = OrangeDefault),
        enabled = isEnable,
        onClick = {
            isPressed.value = true
            onClick()
        },
        modifier = modifier
            .bounceClick()
            .then(
                if (isEnable) Modifier
                    .innerShadow(
                        color = GreyDark,
                        bordersRadius = 25.dp,
                        blurRadius = 4.dp,
                        offsetX = 0.dp,
                        offsetY = 4.dp,
                        spread = 0.dp,
                    )
                    .clip(RoundedCornerShape(25.dp))
                else Modifier
            ),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
    ){
        icon?.let {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = WhiteDefault,
                modifier = Modifier
                    .size(30.dp)
                    .padding(end = 8.dp)
            )
        }
        image?.let {
            Icon(
                painter = painterResource(image),
                contentDescription = null,
                modifier = Modifier
                    .size(30.dp)
                    .padding(end = 8.dp)
            )
        }
        Text(
            text = text,
            color = WhiteDefault
        )
    }
}

@Composable
fun OuterShadowFilledButton(
    text: String,
    fontSize: TextUnit = 14.sp,
    isEnable: Boolean = true,
    color: Color = OrangeDefault,
    textColor: Color = WhiteDefault,
    tintIcon: Color = WhiteDefault,
    //borderStroke: BorderStroke = BorderStroke(width = 1.dp, color = BrownDark),
    shadowColor: Color = BrownDark,
    blurRadius: Dp = 4.dp,
    offsetX: Dp = 0.dp,
    offsetY: Dp = 4.dp,
    spread: Dp = 0.dp,
    icon: ImageVector ?= null,
    image: Int ?= null,
    onClick: () -> Unit,
                    modifier: Modifier
        ){
//    val isPressed = remember { mutableStateOf(false) }
            Button(
                colors = ButtonDefaults.buttonColors(containerColor = color),
                //border = borderStroke,
                onClick = onClick,
                enabled = isEnable,
                modifier = modifier
                    .then(
                        if (isEnable) Modifier
                            .outerShadow(
                                color = shadowColor,
                                bordersRadius = 25.dp,
                                blurRadius = blurRadius,
                                offsetX = offsetX,
                                offsetY = offsetY,
                                spread = spread,
                            ) else modifier
                    ),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
            ){
                icon?.let {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = tintIcon,
                        modifier = Modifier
                            .size(30.dp)
                            .padding(end = 8.dp)
                    )
                }
                image?.let {
                    Image(
                        painter = painterResource(image),
                        contentDescription = null,
                        modifier = Modifier
                            .size(30.dp)
                            .padding(end = 8.dp)
                    )
                }
                Text(
                    text = text,
                    fontSize = fontSize,
                    color = textColor
        )
    }
}

@Composable
fun NormalButtonWithIcon(
    text: String,
    fontSize: TextUnit = 16.sp,
    isEnable: Boolean = true,
    color: Color = OrangeDefault,
    textColor: Color = WhiteDefault,
    tintIcon: Color = WhiteDefault,
    icon: ImageVector ?= null,
    image: Int ?= null,
    onClick: () -> Unit,
    shape: RoundedCornerShape = RoundedCornerShape(8.dp),
    modifier: Modifier,
    spacer: Boolean = true
){
    Button(
        colors = ButtonDefaults.buttonColors(containerColor = color),
        onClick = onClick,
        enabled = isEnable,
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.wrapContentWidth()
        ) {
            Text(
                text = text,
                fontSize = fontSize,
                color = textColor,
            )
            if(spacer == true)
                Spacer(modifier = Modifier.weight(1f)) // đẩy icon sang cuối

            icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    tint = tintIcon,
                    modifier = Modifier.size(24.dp)
                )
            }

            image?.let {
                Icon(
                    painter = painterResource(image),
                    contentDescription = null,
                    modifier = Modifier.size(30.dp)
                )
            }
        }
    }
}

@Composable
fun BasicOutlinedButton(
    text: String,
    foregroundColor: Color = BrownDefault,
    icon: ImageVector ?= null,
    image: Int ?= null,
    url: String ?= null,
    isEnable: Boolean = true,
    onClick: () -> Unit,
    modifier: Modifier
){
    val bordercolor = if (isEnable) foregroundColor else WhiteDefault
    OutlinedButton(
        colors = ButtonDefaults.buttonColors(containerColor = Transparent),
        border = BorderStroke( width = 2.dp, color = bordercolor),
        enabled = isEnable,
        onClick = onClick,
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
//        interactionSource = interactionSource
    ){
        icon?.let {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = BrownDefault,
                modifier = Modifier
                    .size(30.dp)
                    .padding(end = 8.dp)
            )
        }
        image?.let {
            Image(
                painter = painterResource(image),
                contentDescription = null,
                modifier = Modifier
                    .size(30.dp)
                    .padding(end = 8.dp)
            )
        }
        url?.let {
            AsyncImage(
            model = url ,
            contentDescription = null,
            modifier = Modifier
                .size(30.dp)
                .padding(end = 8.dp)
        ) }
        Text(
            text = text,
            color = foregroundColor
        )
    }
}

@Composable
fun CircleIconButton(
    backgroundColor: Color ?= null,
    foregroundColor: Color ?= null,
    icon: ImageVector ?= null,
    image: Int ?= null,
    isEnable: Boolean = true,
    shadow: String ?= null,
    isBadges: Boolean = false,
    badgesCount: Int = 0,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
){
    BadgedBox(
        badge = {
            if (isBadges && badgesCount > 0) {
                Badge {
                    Text(
                        text = if (badgesCount <= 99)badgesCount.toString() else "99+",
                    )
                }
            }
        },
        modifier = modifier.wrapContentSize()
    ) {
        IconButton(
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = backgroundColor ?: OrangeDefault
            ),
            enabled = isEnable,
            onClick = onClick,
            modifier = Modifier
                .size(40.dp)
                .focusable(false)
                .then(
                    if (shadow.equals("inner")) {
                        Modifier
                            .innerShadow(
                                color = GreyDark,
                                bordersRadius = 25.dp,
                                blurRadius = 4.dp,
                                offsetX = 0.dp,
                                offsetY = 4.dp,
                                spread = 0.dp,
                            )
                            .clip(RoundedCornerShape(25.dp))
                    } else if (shadow.equals("outer")) {
                        Modifier.outerShadow(
                            color = GreyDark,
                            bordersRadius = 25.dp,
                            blurRadius = 4.dp,
                            offsetX = 0.dp,
                            offsetY = 4.dp,
                            spread = 0.dp,
                        )
                    } else Modifier
                ),
        ) {
            icon?.let {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = foregroundColor ?: WhiteDefault,
                    modifier = Modifier.size(30.dp)
                )
            }
            image?.let {
                Icon(
                    painter = painterResource(image),
                    contentDescription = null,
                    tint = foregroundColor ?: WhiteDefault,
                    modifier = Modifier.size(30.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun QuantitySelectionButton(
    count: Int,
    onValueIncr: () -> Unit = {},
    onValueDecr: () -> Unit = {},
    modifier: Modifier = Modifier// Callback để cập nhật giá trị bên ngoài
) {
    Box(modifier = modifier
        .outerShadow() // Đổ bóng lên nền
        .background(OrangeDefault, shape = RoundedCornerShape(50))
    ) {
        Row(
            modifier = Modifier
                .padding(0.dp)
                .fillMaxHeight()
                .wrapContentWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Nút Giảm (-)
            IconButton(
                onClick = { if (count > 1) onValueDecr()},
                modifier = Modifier.wrapContentSize()
            ){
                Icon(
                    imageVector = Icons.Filled.Remove,
                    contentDescription = "Decrease",
                    tint = WhiteDefault,
                    modifier = Modifier.wrapContentSize()
                )
            }
            // Hiển thị số lượng
            Box(
                contentAlignment = Alignment.BottomCenter,
                modifier = Modifier
                    .background(Color.White, shape = RoundedCornerShape(50))
                    .wrapContentWidth()
                    .fillMaxHeight()
            ){
                Text(
                    text = "$count",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = OrangeDefault,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(vertical = 5.dp)
                        .fillMaxHeight()
                        .wrapContentWidth()
                        .defaultMinSize(minWidth = 30.dp, minHeight = 30.dp) // Đảm bảo nút có kích thước tối thiểu
                )
            }
            // Nút Tăng (+)
            IconButton(
                onClick = { if (count < 99) onValueIncr()},
                modifier = Modifier.wrapContentSize()
            ){
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Increase",
                    tint = WhiteDefault,
                    modifier = Modifier.wrapContentSize()
                )
            }
        }
    }
}

@Composable
fun ProductClientListItem(
    item: ProductResponse,
    onClick: (ProductResponse) -> Unit =  {ProductResponse ->},
    color: Color = WhiteDefault,
    modifier: Modifier = Modifier){
    Card(
        enabled = item.isAvailable,
        colors = CardColors(color, BrownDefault, GreyLight.copy(0.5f), GreyDark.copy(0.5f)),
        onClick = { onClick(item) },
//        elevation = CardDefaults.cardElevation(
//            defaultElevation = 4.dp,
//        ),
        modifier = Modifier
            .height(80.dp)
            .fillMaxWidth(0.9f)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier
                .fillMaxSize()
        ) {
            AsyncImage(
                model = item.getRealURL(), // Đây là URL từ API
                contentDescription = null,
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.ic_mam_logo),
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(
                horizontalAlignment = Alignment.Start,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(
                    text = item.name,
                    textAlign = TextAlign.Start,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(top = 5.dp)
                )
                Text(
                    text = item.shortDescription,
                    textAlign = TextAlign.Start,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(bottom = 5.dp)
                )
                Text(
                    text = if (item.isAvailable) item.getPriceToString() else "Hết sản phẩm",
                    textAlign = TextAlign.Start,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(bottom = 5.dp)
                )
            }
        }
    }
}

@Composable
fun CustomRadioButton(
    selectedColor: Color = BrownDefault,
    unSelectedColor: Color = BrownLight,
    foregroundColor: Color = WhiteDefault,
    selectedIcon: ImageVector ?= null,
    selected: Boolean,
    onClick: () -> Unit,
    text: String,
    modifier: Modifier,
) {
    val backgroundColor = if (selected) selectedColor else unSelectedColor // Màu nâu
    val textColor = foregroundColor
    val icon = if (selected) selectedIcon else null

    Row(
        modifier = Modifier
            .background(backgroundColor, shape = RoundedCornerShape(50.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            color = textColor,
            fontWeight = if(selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun RadioOption(
    modifier: Modifier = Modifier,
    title: String,
    options: List<VariationOptionResponse>,
    defaultOption: VariationOptionResponse = options.first() ,
    onClick: (VariationOptionResponse) -> Unit,
) {
    var temp by remember { mutableStateOf(defaultOption) }
    Column(
        modifier = modifier.wrapContentHeight()
    ) {
        Text(
            text = title,
            textAlign = TextAlign.Start,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            color = BrownDefault,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(vertical = 10.dp)
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            items(options) { option ->
                CustomRadioButton(
                    text = option.value,
                    selected = option == temp,
                    selectedIcon = Icons.Default.Check,
                    onClick = {
                        temp = option
                        onClick(option) },
                    modifier = Modifier
                )
            }
        }
    }
}

@Composable
fun PizzaSizeOption(
    modifier: Modifier = Modifier,
    title: String,
    options: List<VariationOptionResponse>,
    defaultOption: VariationOptionResponse = options.first() ,
    @DrawableRes image: Int,
    onClick: (VariationOptionResponse) -> Unit,
    ) {
        val scope = rememberCoroutineScope()
        val state = rememberLazyListState()
        var temp by remember { mutableStateOf(defaultOption) }
        Column(
            modifier = modifier.wrapContentHeight(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                textAlign = TextAlign.Start,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                color = BrownDefault,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(vertical = 10.dp)
                    .fillMaxWidth()
            )
            LazyRow(
                state = state,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .width(200.dp)
                    .wrapContentHeight()
                    .padding(bottom = 10.dp)
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) { // Always consume pointer events
                                val event = awaitPointerEvent()
                                event.changes.forEach { it.consume() }
                            }
                        }
                    }
            ) {
                items(options){
                    val size: Double = (it.value.filter { it.isDigit() }.toDouble() * 3 + 15)/ 100
                    Box(
                        modifier = Modifier.size(200.dp),
                        contentAlignment = Alignment.Center
                    ){
                        Image(
                            painter = painterResource(image),
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize(size.toFloat())
                        )
                    }
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .wrapContentWidth()
                    .wrapContentHeight()
                    .background(BrownLight, shape = RoundedCornerShape(50.dp))
            ) {
                options.forEach() { option ->
                    CustomRadioButton(
                        text = option.value,
                        selected = option == temp,
                        onClick = {
                            temp = option
                            scope.launch {
                                state.animateScrollToItem(options.indexOf(option))
                            }
                            onClick(option) },
                        modifier = Modifier
                    )
                }
            }
        }
}

@Composable
fun CustomToggleButton(
    modifier: Modifier = Modifier,
    text: String = "",
    onSelect: () -> Unit,
    onUnselect: () -> Unit,
) {
    // Define the colors based on the toggle state

    var isSelected: Boolean by remember { mutableStateOf(false) }
    val backgroundColor = if (isSelected) BrownDefault else BrownLight
    val textColor = WhiteDefault
    // Create the button
    Box(
        modifier = modifier
            .background(backgroundColor, shape = RoundedCornerShape(50.dp))
            .clickable {
                isSelected = !isSelected
                if (isSelected) onSelect()
                else onUnselect()
            }
            .padding(horizontal = 10.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontWeight = if(isSelected)FontWeight.Bold else FontWeight.Normal,
            fontSize = 16.sp
        )
    }
}

@Composable
fun MultiChoiceOption(
    modifier: Modifier = Modifier,
    title: String,
    options: List<VariationOptionResponse>,
    onSelect: (VariationOptionResponse) -> Unit,
    onUnselect: (VariationOptionResponse) -> Unit,
) {
    Column(
        modifier = modifier.wrapContentHeight()
    ) {
        Text(
            text = title,
            textAlign = TextAlign.Start,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            color = BrownDefault,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(vertical = 10.dp)
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            items(options) { option ->
                CustomToggleButton(
                    text = option.value,
                    onSelect = { onSelect(option) },
                    onUnselect = { onUnselect(option) }
                )
            }
        }
    }
}
@Preview
@Composable
fun ButtonPreview(){

}
