package com.example.mam.navigation

import android.net.Uri
import android.util.Log
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.example.mam.screen.authentication.ForgetPasswordScreen
import com.example.mam.screen.authentication.OTPScreen
import com.example.mam.screen.authentication.RecoveryCodeScreen
import com.example.mam.screen.authentication.SetupTwoFaScreen
import com.example.mam.screen.authentication.SignInScreen
import com.example.mam.screen.authentication.SignUpScreen
import com.example.mam.screen.authentication.StartScreen
import com.example.mam.screen.authentication.VerifyTwoFaScreen
import com.example.mam.screen.client.CartScreen
import com.example.mam.screen.client.CheckOutScreen
import com.example.mam.screen.client.HomeScreen
import com.example.mam.screen.client.ItemScreen
import com.example.mam.screen.client.MapScreen
import com.example.mam.screen.client.OrderHistoryScreen
import com.example.mam.screen.client.OrderScreen
import com.example.mam.screen.client.PaymentResultScreen
import com.example.mam.screen.client.ProfileScreen
import com.example.mam.screen.client.SearchScreen
import com.example.mam.screen.management.DashboardScreen
import com.example.mam.screen.management.ListCategoryScreen
import com.example.mam.screen.management.ListNotificationScreen
import com.example.mam.screen.management.ListOrderScreen
import com.example.mam.screen.management.ListProductScreen
import com.example.mam.screen.management.ListPromotionScreen
import com.example.mam.screen.management.ListShipperScreen
import com.example.mam.screen.management.ListUserScreen
import com.example.mam.screen.management.ManageCategoryScreen
import com.example.mam.screen.management.ManageNotificationScreen
import com.example.mam.screen.management.ManageOrderScreen
import com.example.mam.screen.management.ManageProductScreen
import com.example.mam.screen.management.ManagePromotionScreen
import com.example.mam.screen.management.ManageShipperScreen
import com.example.mam.screen.management.ManageUserScreen
import com.example.mam.viewmodel.authentication.ForgetPasswordViewModel
import com.example.mam.viewmodel.client.NotificationViewModel
import com.example.mam.viewmodel.authentication.SignInViewModel
import com.example.mam.viewmodel.authentication.SignUpViewModel
import com.example.mam.viewmodel.authentication.StartViewModel
import com.example.mam.viewmodel.client.CartViewModel
import com.example.mam.viewmodel.client.CheckOutViewModel
import com.example.mam.viewmodel.client.HomeScreenViewModel
import com.example.mam.viewmodel.client.ItemViewModel
import com.example.mam.viewmodel.client.OrderHistoryViewModel
import com.example.mam.viewmodel.client.OrderViewModel
import com.example.mam.viewmodel.client.ProfileViewModel
import com.example.mam.viewmodel.client.SearchViewModel
import com.example.mam.viewmodel.management.DashboardViewModel
import com.example.mam.viewmodel.management.ListCategoryViewModel
import com.example.mam.viewmodel.management.ListNotificationViewModel
import com.example.mam.viewmodel.management.ListOrderViewModel
import com.example.mam.viewmodel.management.ListProductViewModel
import com.example.mam.viewmodel.management.ListPromotionViewModel
import com.example.mam.viewmodel.management.ListShipperViewModel
import com.example.mam.viewmodel.management.ListUserViewModel
import com.example.mam.viewmodel.management.ManageCategoryViewModel
import com.example.mam.viewmodel.management.ManageNotificationViewModel
import com.example.mam.viewmodel.management.ManageOrderViewModel
import com.example.mam.viewmodel.management.ManageProductViewModel
import com.example.mam.viewmodel.management.ManagePromotionViewModel
import com.example.mam.viewmodel.management.ManageShipperViewModel
import com.example.mam.viewmodel.management.ManageUserViewModel
import com.example.mam.viewmodel.twofa.TwoFaViewModel
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.plcoding.composeotpinput.OtpViewModel
import com.yourapp.ui.notifications.NotificationScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job


@Composable
@OptIn(ExperimentalAnimationApi::class)
fun MainNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = "Authentication",
    //deepLinkIntent: Intent? = null
    uri : Uri? = null
) {


    val unAuthorize by AuthEventManager.unauthorizedEvent.collectAsStateWithLifecycle()
    LaunchedEffect(unAuthorize){
        if (unAuthorize) {
            Log.d("AUTH", "Unauthorized access detected, navigating to StartScreen")
            navController.navigate(AuthenticationScreen.Start.name) {
                popUpTo(AuthenticationScreen.Start.name) { inclusive = true }
                launchSingleTop = true
            }
            AuthEventManager.resetUnauthorized() // Reset the event
        }
    }

    val coroutineScope = CoroutineScope(Job() + Dispatchers.IO)
    AnimatedNavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(
            route = "payment-result",
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "mam://payment/result"
                }
            ),
            ) { backStackEntry ->
            val allParams = uri?.queryParameterNames?.joinToString(", ") { "$it=${uri.getQueryParameter(it)}" } ?: "No parameters"
            val keys = backStackEntry.savedStateHandle.keys()
            PaymentResultScreen(
                onBackHome = {
                    navController.navigate(route = HomeScreen.HomeSreen.name) {
                        popUpTo("Home") { inclusive = true }
                    }
                },
                uri = uri,
                params = allParams
            )
        }
        navigation(
            startDestination = AuthenticationScreen.Start.name,
            route = "Authentication"
        ) {
            composable(
                route = AuthenticationScreen.Start.name,
                enterTransition = defaultTransitions(),
                exitTransition = defaultExitTransitions(),
                popEnterTransition = defaultPopEnterTransitions(),
                popExitTransition = defaultPopExitTransitions()
            ) { backStackEntry ->
                val startVM: StartViewModel = viewModel(backStackEntry, factory = StartViewModel.Factory)
                StartScreen(
                    viewModel = startVM,
                    onSignInClicked = {
                        navController.navigate(AuthenticationScreen.SignIn.name)
                    },
                    onAutoSignIn = {
                        navController.navigate(route = "Home") {
                            popUpTo("Authorization") { inclusive = true }
                        }
                    },
                    onSignInManager = {
                        navController.navigate(route = "Dashboard") {
                            popUpTo("Authorization") { inclusive = true }
                        }
                    },
                    onSignUpClicked = {
                        navController.navigate(AuthenticationScreen.SignUp.name)
                    },
                )
            }

            composable(
                route = AuthenticationScreen.SignIn.name,
                enterTransition = defaultTransitions(),
                exitTransition = defaultExitTransitions(),
                popEnterTransition = defaultPopEnterTransitions(),
                popExitTransition = defaultPopExitTransitions()
            ) { backStackEntry ->
                val signInVM: SignInViewModel = viewModel(backStackEntry, factory = SignInViewModel.Factory)
                SignInScreen(
                    viewModel = signInVM,
                    onSignInClicked = {
                        navController.navigate(route = "Home") {
                            popUpTo("Authorization") { inclusive = true }
                        }
                    },
                    onSignInManager = {
                        navController.navigate(route = "Dashboard") {
                            popUpTo("Authorization") { inclusive = true }
                        }
                    },
                    onForgotClicked = {
                        navController.navigate(AuthenticationScreen.ForgetPW.name)
                    },
                    onBackClicked = {
                        navController.popBackStack()
                    },
                )
            }

            composable(
                route = AuthenticationScreen.SignUp.name,
                enterTransition = defaultTransitions(),
                exitTransition = defaultExitTransitions(),
                popEnterTransition = defaultPopEnterTransitions(),
                popExitTransition = defaultPopExitTransitions()
            ) { backStackEntry ->
                val signUpVM: SignUpViewModel = viewModel(backStackEntry, factory = SignUpViewModel.Factory)
                SignUpScreen(
                    onSignInClicked = {
                        navController.navigate(AuthenticationScreen.SignIn.name)
                    },
                    onBackClicked = {
                        navController.popBackStack()
                    },
                )
            }

            composable(
                route = AuthenticationScreen.ForgetPW.name,
                enterTransition = defaultTransitions(),
                exitTransition = defaultExitTransitions(),
                popEnterTransition = defaultPopEnterTransitions(),
                popExitTransition = defaultPopExitTransitions()
            ) { backStackEntry ->
                val forgetPasswordVM: ForgetPasswordViewModel = viewModel( factory = ForgetPasswordViewModel.Factory)
                ForgetPasswordScreen(
                    onChangeClicked = { email, password ->
                        navController.navigate("${AuthenticationScreen.OTP.name}/$email/$password")
                    },
                    onCloseClicked = {
                        navController.popBackStack()
                    },
                    viewModel = forgetPasswordVM,
                    isForgot = true,
                )
            }
            composable(
                route = "ChangePassword",
                enterTransition = defaultTransitions(),
                exitTransition = defaultExitTransitions(),
                popEnterTransition = defaultPopEnterTransitions(),
                popExitTransition = defaultPopExitTransitions()
            ) { backStackEntry ->
                val forgetPasswordVM: ForgetPasswordViewModel = viewModel( factory = ForgetPasswordViewModel.Factory)
                ForgetPasswordScreen(
                    isForgot = false,
                    onCloseClicked = {
                        navController.popBackStack()
                    },
                    viewModel = forgetPasswordVM,
                )
            }
            composable(
                route = "${AuthenticationScreen.OTP.name}/{email}/{password}",
                arguments = listOf(navArgument("email") { type = NavType.StringType },
                                   navArgument("password") { type = NavType.StringType }),
                enterTransition = defaultTransitions(),
                exitTransition = defaultExitTransitions(),
                popEnterTransition = defaultPopEnterTransitions(),
                popExitTransition = defaultPopExitTransitions()
            ) {backStackEntry ->
                val viewModel: OtpViewModel = viewModel(backStackEntry,factory = OtpViewModel.Factory)
                OTPScreen(
                    onVerifyClicked = {
                        navController.navigate(route = AuthenticationScreen.Start.name) {
                            popUpTo(AuthenticationScreen.OTP.name) { inclusive = true }
                        }
                    },
                    onCloseClicked = {
                        navController.popBackStack()
                    },
                    viewModel = viewModel,
                )
            }
            composable(
                route = AuthenticationScreen.TwoFaSetup.name,
                enterTransition = defaultTransitions(),
                exitTransition = defaultExitTransitions(),
                popEnterTransition = defaultPopEnterTransitions(),
                popExitTransition = defaultPopExitTransitions()
            ) { backStackEntry ->
                // Khởi tạo ViewModel (Giả sử bạn đã tạo Factory như các màn hình khác)
                val viewModel: TwoFaViewModel = viewModel(backStackEntry, factory = TwoFaViewModel.Factory)

                SetupTwoFaScreen(
                    viewModel = viewModel,
                    onBackClicked = { navController.popBackStack() },
                    onNextClicked = { secretKey ->
                        // Chuyển sang màn Verify, truyền theo secretKey nếu cần thiết
                        // Hoặc nếu dùng chung ViewModel thì không cần truyền
                        navController.navigate("${AuthenticationScreen.TwoFaVerify.name}/$secretKey")
                    }
                )
            }

            composable(
                route = "${AuthenticationScreen.TwoFaVerify.name}/{secretKey}", // Nhận secretKey từ màn trước
                arguments = listOf(navArgument("secretKey") { type = NavType.StringType }),
                enterTransition = defaultTransitions(),
                exitTransition = defaultExitTransitions(),
                popEnterTransition = defaultPopEnterTransitions(),
                popExitTransition = defaultPopExitTransitions()
            ) { backStackEntry ->
                val viewModel: TwoFaViewModel = viewModel(backStackEntry, factory = TwoFaViewModel.Factory)

                // Lấy secretKey từ Argument để ViewModel xử lý
                val secretKey = backStackEntry.arguments?.getString("secretKey") ?: ""
                LaunchedEffect(secretKey) {
                    viewModel.setSecretKey(secretKey)
                }

                VerifyTwoFaScreen(
                    viewModel = viewModel,
                    onBackClicked = { navController.popBackStack() },
                    onSuccess = {
                        // Xác thực thành công -> Sang màn lưu mã khôi phục
                        navController.navigate(AuthenticationScreen.TwoFaRecovery.name) {
                            // Xóa màn hình Setup và Verify khỏi backstack để user không back lại được
                            popUpTo(AuthenticationScreen.TwoFaSetup.name) { inclusive = true }
                        }
                    }
                )
            }

            composable(
                route = AuthenticationScreen.TwoFaRecovery.name,
                enterTransition = defaultTransitions(),
                exitTransition = defaultExitTransitions(),
                popEnterTransition = defaultPopEnterTransitions(),
                popExitTransition = defaultPopExitTransitions()
            ) { backStackEntry ->
                val viewModel: TwoFaViewModel = viewModel(backStackEntry, factory = TwoFaViewModel.Factory)

                RecoveryCodeScreen(
                    viewModel = viewModel,
                    onDoneClicked = {
                        // Hoàn tất -> Về màn hình Profile hoặc Home
                        navController.navigate("Profile") {
                            // Xóa toàn bộ luồng 2FA khỏi backstack
                            popUpTo("Profile") { inclusive = true }
                        }
                    }
                )
            }
        }
        navigation(
            startDestination = HomeScreen.HomeSreen.name,
            route = "Home"
        ){
            composable(
                route = HomeScreen.HomeSreen.name,
                enterTransition = defaultTransitions(),
                exitTransition = defaultExitTransitions(),
                popEnterTransition = defaultPopEnterTransitions(),
                popExitTransition = defaultPopExitTransitions()
            ) { backStackEntry ->
                val viewmodel: HomeScreenViewModel = viewModel(backStackEntry, factory = HomeScreenViewModel.Factory)
                HomeScreen(
                    onItemClicked = { item ->
                        navController.navigate("Details/${item.id}")
                    },
                    onSearchClicked = {navController.navigate(HomeScreen.Search.name)},
                    onCartClicked = { navController.navigate("Cart") },
                    onShippingClicked = {navController.navigate("OrderHistory")},
                    onNotificationClicked = {navController.navigate("Notification")},
                    onProfileClicked = {navController.navigate("Profile")},
                    viewmodel = viewmodel
                )
            }
            composable(
                route = "Notification",
                enterTransition = defaultTransitions(),
                exitTransition = defaultExitTransitions(),
                popEnterTransition = defaultPopEnterTransitions(),
                popExitTransition = defaultPopExitTransitions()
            ) { backStackEntry ->
                val viewModel: NotificationViewModel = viewModel(backStackEntry, factory = NotificationViewModel.Factory)
                NotificationScreen(
                    onBackClicked = {navController.popBackStack()},
                    viewModel = viewModel
                )
            }
            composable(
                route = "Profile",
                enterTransition = defaultTransitions(),
                exitTransition = defaultExitTransitions(),
                popEnterTransition = defaultPopEnterTransitions(),
                popExitTransition = defaultPopExitTransitions()
            ) { backStackEntry ->
                val viewModel: ProfileViewModel = viewModel(backStackEntry, factory = ProfileViewModel.Factory)
                ProfileScreen(
                    onBackClicked = {navController.popBackStack()},
                    onLogoutClicked = {
                        navController.navigate(route = AuthenticationScreen.Start.name) {
                        popUpTo("Profile") { inclusive = true }
                    }},
                    onChangePasswordClicked = {
                        navController.navigate("ChangePassword")
                    },
                    onHistoryClicked = {
                        navController.navigate("OrderHistory")
                    },
                    viewModel = viewModel
                )
            }
            composable(
                route = "Details/{itemId}",
                arguments = listOf(navArgument("itemId") { type = NavType.LongType }),
                enterTransition = defaultTransitions(),
                exitTransition = defaultExitTransitions(),
                popEnterTransition = defaultPopEnterTransitions(),
                popExitTransition = defaultPopExitTransitions()
            ) {backStackEntry ->
                val viewModel: ItemViewModel = viewModel(backStackEntry, factory = ItemViewModel.Factory)
                ItemScreen(
                    onBackClicked = {navController.popBackStack()},
                    onAddClick = { },
                    onCartClicked = {navController.navigate("Cart")},
                    viewModel = viewModel
                )
            }
            composable(
                route = HomeScreen.Search.name,
                enterTransition = defaultTransitions(),
                exitTransition = defaultExitTransitions(),
                popEnterTransition = defaultPopEnterTransitions(),
                popExitTransition = defaultPopExitTransitions()
            ) {backStackEntry ->
                val viewModel: SearchViewModel = viewModel(backStackEntry, factory = SearchViewModel.Factory)
                SearchScreen(
                    onItemClicked = { item ->
                        navController.navigate("Details/${item.id}")
                    },
                    onBackClicked = {navController.popBackStack()},
                    viewModel = viewModel
                )
            }
            composable(
                route = "Cart",
                enterTransition = defaultTransitions(),
                exitTransition = defaultExitTransitions(),
                popEnterTransition = defaultPopEnterTransitions(),
                popExitTransition = defaultPopExitTransitions()
            ) { backStackEntry ->
                val viewModel: CartViewModel = viewModel(backStackEntry, factory = CartViewModel.Factory)
                CartScreen(
                    onBackClicked = {navController.popBackStack()},
                    onAdditionalProductClicked = { item ->
                        navController.navigate("Details/${item}")
                    },
                    onCheckOutClicked = {navController.navigate(("CheckOut"))},
                    viewModel = viewModel
                )
            }
            composable(
                route = "CheckOut",
                enterTransition = defaultTransitions(),
                exitTransition = defaultExitTransitions(),
                popEnterTransition = defaultPopEnterTransitions(),
                popExitTransition = defaultPopExitTransitions(),
            ) { backStackEntry ->
                val viewModel: CheckOutViewModel = viewModel(backStackEntry, factory = CheckOutViewModel.Factory)
                CheckOutScreen(
                    onBackClicked = {navController.popBackStack()},
                    onHomeClicked = {
                        //order
                        navController.navigate(route = HomeScreen.HomeSreen.name) {
                            popUpTo("CheckOut") { inclusive = true }
                        } },
                    onCheckOutClicked = {
                            orderId ->
                        navController.navigate("Order/${orderId}") },
                    onChangeAddressClicked = {
                        navController.navigate("Address")
                        // get address for saveStateHandle
                        navController.previousBackStackEntry?.savedStateHandle?.getLiveData<String>("address")?.observe(
                            backStackEntry
                        ) { address ->
                            viewModel.setAddress(address)
                        }
                        navController.previousBackStackEntry?.savedStateHandle?.getLiveData<Double>("latitude")?.observe(
                            backStackEntry
                        ) { latitude ->
                            viewModel.setLatitude(latitude)
                        }
                        navController.previousBackStackEntry?.savedStateHandle?.getLiveData<Double>("longitude")?.observe(
                            backStackEntry
                        ) { longitude ->
                            viewModel.setLongitude(longitude)
                            viewModel.setAdressAndCoordinates()
                        }
                    },
                    viewModel = viewModel
                )
            }

            composable(
                route = "Address",
                enterTransition = defaultTransitions(),
                exitTransition = defaultExitTransitions(),
                popEnterTransition = defaultPopEnterTransitions(),
                popExitTransition = defaultPopExitTransitions()
            ){
                MapScreen(
                    onBackClicked = {navController.popBackStack()},
                    onSelectClicked = { address, latitude, longitude ->
                        navController.previousBackStackEntry?.savedStateHandle?.set("address", address)
                        navController.previousBackStackEntry?.savedStateHandle?.set("latitude", latitude)
                        navController.previousBackStackEntry?.savedStateHandle?.set("longitude", longitude)
                        navController.popBackStack()
                    },
                )
            }
            composable(
                route = "Order/{orderId}",
                arguments = listOf(navArgument("orderId") { type = NavType.LongType }),
                enterTransition = defaultTransitions(),
                exitTransition = defaultExitTransitions(),
                popEnterTransition = defaultPopEnterTransitions(),
                popExitTransition = defaultPopExitTransitions()
            ){ backStackEntry ->
                val viewModel: OrderViewModel = viewModel(backStackEntry, factory = OrderViewModel.Factory)
                OrderScreen(
                    onBackClicked = {
                        navController.navigate(route = HomeScreen.HomeSreen.name) {
                        popUpTo("Home") { inclusive = true }
                    }},
                    viewModel = viewModel
                )
            }
            composable(
                route = "OrderHistory",
                enterTransition = defaultTransitions(),
                exitTransition = defaultExitTransitions(),
                popEnterTransition = defaultPopEnterTransitions(),
                popExitTransition = defaultPopExitTransitions()
            ){ backStackEntry ->
                val viewModel: OrderHistoryViewModel = viewModel(backStackEntry, factory = OrderHistoryViewModel.Factory)
                OrderHistoryScreen(
                    onBackClicked = {navController.popBackStack()},
                    onClick = { orderId ->
                        navController.navigate("Order/${orderId}")
                    },
                    viewModel = viewModel
                )
            }
            composable(
                route = "Dashboard",
                enterTransition = defaultTransitions(),
                exitTransition = defaultExitTransitions(),
                popEnterTransition = defaultPopEnterTransitions(),
                popExitTransition = defaultPopExitTransitions()
            ){ backStackEntry ->
                val viewModel: DashboardViewModel = viewModel(backStackEntry, factory = DashboardViewModel.Factory)
                DashboardScreen(
                    onProfileClicked = {
                        navController.navigate("AdminProfile")
                    },
                    onItemClicked = { item ->
                        when(item) {
                            "Danh mục" -> navController.navigate("ListCategory")
                            "Sản phẩm" -> navController.navigate("ListProduct")
                            "Người dùng" -> navController.navigate("ListUser")
                            "Shipper" -> navController.navigate("ListShipper")
                            "Thông báo" -> navController.navigate("ListNotification")
                            "Khuyến mãi" -> navController.navigate("ListPromotion")
                            "Đơn hàng" -> navController.navigate("ListOrder")
                        }
                    },
                    onProcessingOrderClick = {
                        navController.navigate("ProcessingOrder")
                    },
                    onPreProcessOrderClick = {
                        navController.navigate("PreProcessingOrder")
                    },
                    viewModel = viewModel
                )
            }
            composable(
                route = "ProcessingOrder",
                enterTransition = defaultTransitions(),
                exitTransition = defaultExitTransitions(),
                popEnterTransition = defaultPopEnterTransitions(),
                popExitTransition = defaultPopExitTransitions()
            ) { backStackEntry ->
                val viewModel: ListOrderViewModel = viewModel(backStackEntry, factory = ListOrderViewModel.Factory)
                ListOrderScreen(
                    onBackClick = {navController.popBackStack()},
                    onEditOrderClick = { orderId ->
                        navController.navigate("EditOrder/${orderId}")
                    },
                    onHomeClick = {
                        navController.navigate("Dashboard") {
                            popUpTo("Dashboard") { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    isProcessing = true,
                    viewModel = viewModel
                )
            }
            composable(
                route = "PreProcessingOrder",
                enterTransition = defaultTransitions(),
                exitTransition = defaultExitTransitions(),
                popEnterTransition = defaultPopEnterTransitions(),
                popExitTransition = defaultPopExitTransitions()
            ) { backStackEntry ->
                val viewModel: ListOrderViewModel = viewModel(backStackEntry, factory = ListOrderViewModel.Factory)
                ListOrderScreen(
                    onBackClick = {navController.popBackStack()},
                    onEditOrderClick = { orderId ->
                        navController.navigate("EditOrder/${orderId}")
                    },
                    onHomeClick = {
                        navController.navigate("Dashboard") {
                            popUpTo("Dashboard") { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    isPreProcessing = true,
                    viewModel = viewModel
                )
            }
            composable(
                route = "AdminProfile",
                enterTransition = defaultTransitions(),
                exitTransition = defaultExitTransitions(),
                popEnterTransition = defaultPopEnterTransitions(),
                popExitTransition = defaultPopExitTransitions()
            ) { backStackEntry ->
                val viewModel: ProfileViewModel = viewModel(backStackEntry, factory = ProfileViewModel.Factory)
                ProfileScreen(
                    onBackClicked = {navController.popBackStack()},
                    onLogoutClicked = {
                        navController.navigate(route = AuthenticationScreen.Start.name) {
                            popUpTo("Profile") { inclusive = true }
                        }},
                    onChangePasswordClicked = {
                        navController.navigate("ChangePassword")
                    },
                    onHistoryClicked = {
                        navController.navigate("OrderHistory")
                    },
                    isAdmin = true,
                    viewModel = viewModel
                )
            }
            composable(
                route = "ListCategory",
                enterTransition = defaultTransitions(),
                exitTransition = defaultExitTransitions(),
                popEnterTransition = defaultPopEnterTransitions(),
                popExitTransition = defaultPopExitTransitions()
            ) { backStackEntry ->
                val viewModel: ListCategoryViewModel = viewModel(backStackEntry, factory = ListCategoryViewModel.Factory)
                ListCategoryScreen(
                    onBackClick = {navController.popBackStack()},
                    onAddCategoryClick = {
                        navController.navigate("AddCategory")
                    },
                    onEditCategoryClick = { categoryId ->
                        navController.navigate("EditCategory/${categoryId}")
                    },
                    onHomeClick = {
                        navController.navigate("Dashboard") {
                            popUpTo("Dashboard") { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    viewModel = viewModel
                )
            }
            composable(
                route = "AddCategory",
                enterTransition = defaultTransitions(),
                exitTransition = defaultExitTransitions(),
                popEnterTransition = defaultPopEnterTransitions(),
                popExitTransition = defaultPopExitTransitions()
            ) { backStackEntry ->
                val viewModel: ManageCategoryViewModel = viewModel(backStackEntry, factory = ManageCategoryViewModel.Factory)
                ManageCategoryScreen(
                    viewModel = viewModel,
                    onBackClick = {navController.popBackStack()},
                    isAdd = true,
                    isEdit = false,
                )
            }
            composable(
                route = "EditCategory/{categoryId}",
                arguments = listOf(navArgument("categoryId") { type = NavType.LongType }),
                enterTransition = defaultTransitions(),
                exitTransition = defaultExitTransitions(),
                popEnterTransition = defaultPopEnterTransitions(),
                popExitTransition = defaultPopExitTransitions()
            ) { backStackEntry ->
                val viewModel: ManageCategoryViewModel = viewModel(backStackEntry, factory = ManageCategoryViewModel.Factory)
                ManageCategoryScreen(
                    viewModel = viewModel,
                    onBackClick = {navController.popBackStack()},
                    isAdd = false,
                    isEdit = true,
                )
            }
            composable(
                route = "ListProduct",
                enterTransition = defaultTransitions(),
                exitTransition = defaultExitTransitions(),
                popEnterTransition = defaultPopEnterTransitions(),
                popExitTransition = defaultPopExitTransitions()
            ) { backStackEntry ->
                val viewModel: ListProductViewModel = viewModel(backStackEntry, factory = ListProductViewModel.Factory)
                ListProductScreen(
                    onBackClick = {navController.popBackStack()},
                    onAddProductClick = {
                        navController.navigate("AddProduct")
                    },
                    onEditProductClick = { product ->
                        navController.navigate("EditProduct/${product}")
                    },
                    onProductClick = { product ->
                        navController.navigate("DetailsProduct/${product}")
                    },
                    onHomeClick = {
                        navController.navigate("Dashboard") {
                            popUpTo("Dashboard") { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    viewModel = viewModel
                )
            }
            composable(
                route = "EditProduct/{productId}",
                arguments = listOf(navArgument("productId") { type = NavType.LongType }),
                enterTransition = defaultTransitions(),
                exitTransition = defaultExitTransitions(),
                popEnterTransition = defaultPopEnterTransitions(),
                popExitTransition = defaultPopExitTransitions()
            ) { backStackEntry ->
                val viewModel: ManageProductViewModel = viewModel(backStackEntry, factory = ManageProductViewModel.Factory)
                ManageProductScreen(
                    viewModel = viewModel,
                    onBackClick = {navController.popBackStack()},
                    isAdd = false,
                    isEdit = true,
                )
            }
            composable(
                route = "DetailsProduct/{productId}",
                arguments = listOf(navArgument("productId") { type = NavType.LongType }),
                enterTransition = defaultTransitions(),
                exitTransition = defaultExitTransitions(),
                popEnterTransition = defaultPopEnterTransitions(),
                popExitTransition = defaultPopExitTransitions()
            ) { backStackEntry ->
                val viewModel: ManageProductViewModel = viewModel(backStackEntry, factory = ManageProductViewModel.Factory)
                ManageProductScreen(
                    viewModel = viewModel,
                    onBackClick = {navController.popBackStack()},
                    isAdd = false,
                    isEdit = false,
                )
            }
            composable(
                route = "AddProduct",
                enterTransition = defaultTransitions(),
                exitTransition = defaultExitTransitions(),
                popEnterTransition = defaultPopEnterTransitions(),
                popExitTransition = defaultPopExitTransitions()
            ) { backStackEntry ->
                val viewModel: ManageProductViewModel = viewModel(backStackEntry, factory = ManageProductViewModel.Factory)
                ManageProductScreen(
                    viewModel = viewModel,
                    onBackClick = {navController.popBackStack()},
                    isAdd = true,
                    isEdit = false,
                )
            }
            composable(
                route = "ListOrder",
                enterTransition = defaultTransitions(),
                exitTransition = defaultExitTransitions(),
                popEnterTransition = defaultPopEnterTransitions(),
                popExitTransition = defaultPopExitTransitions()
            ) { backStackEntry ->
                val viewModel: ListOrderViewModel = viewModel(backStackEntry, factory = ListOrderViewModel.Factory)
                ListOrderScreen(
                    onBackClick = {navController.popBackStack()},
                    onEditOrderClick = { orderId ->
                        navController.navigate("EditOrder/${orderId}")
                    },
                    onHomeClick = {
                        navController.navigate("Dashboard") {
                            popUpTo("Dashboard") { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    viewModel = viewModel
                )
            }
            composable(
                route = "EditOrder/{orderId}",
                arguments = listOf(navArgument("orderId") { type = NavType.LongType }),
                enterTransition = defaultTransitions(),
                exitTransition = defaultExitTransitions(),
                popEnterTransition = defaultPopEnterTransitions(),
                popExitTransition = defaultPopExitTransitions()
            ) {
                backStackEntry ->
                val viewModel: ManageOrderViewModel = viewModel(backStackEntry, factory = ManageOrderViewModel.Factory)
                ManageOrderScreen(
                    onBackClick = {navController.popBackStack()},
                    viewModel = viewModel,
                )

            }
            composable(
                route = "ListNotification",
                enterTransition = defaultTransitions(),
                exitTransition = defaultExitTransitions(),
                popEnterTransition = defaultPopEnterTransitions(),
                popExitTransition = defaultPopExitTransitions()
            ) { backStackEntry ->
                val viewModel: ListNotificationViewModel = viewModel(backStackEntry, factory = ListNotificationViewModel.Factory)
                ListNotificationScreen(
                    onBackClick = {navController.popBackStack()},
                    onAddNotificationClick = {
                        navController.navigate("AddNotification")
                    },
                    onHomeClick = {
                        navController.navigate("Dashboard") {
                            popUpTo("Dashboard") { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    viewModel = viewModel
                )
            }
            composable(
                route = "AddNotification",
                enterTransition = defaultTransitions(),
                exitTransition = defaultExitTransitions(),
                popEnterTransition = defaultPopEnterTransitions(),
                popExitTransition = defaultPopExitTransitions()
            ) { backStackEntry ->
                val viewModel: ManageNotificationViewModel = viewModel(backStackEntry, factory = ManageNotificationViewModel.Factory)
                ManageNotificationScreen(
                    viewModel = viewModel,
                    onBackClick = {navController.popBackStack()},
                )
            }
            composable(
                route = "ListPromotion",
                enterTransition = defaultTransitions(),
                exitTransition = defaultExitTransitions(),
                popEnterTransition = defaultPopEnterTransitions(),
                popExitTransition = defaultPopExitTransitions()
            ) { backStackEntry ->
                val viewModel: ListPromotionViewModel = viewModel(backStackEntry, factory = ListPromotionViewModel.Factory)
                ListPromotionScreen(
                    onBackClick = {navController.popBackStack()},
                    onAddClick = {
                        navController.navigate("AddPromotion")
                    },
                    onHomeClick = {
                        navController.navigate("Dashboard") {
                            popUpTo("Dashboard") { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    viewModel = viewModel
                )
            }
            composable(
                route = "AddPromotion",
                enterTransition = defaultTransitions(),
                exitTransition = defaultExitTransitions(),
                popEnterTransition = defaultPopEnterTransitions(),
                popExitTransition = defaultPopExitTransitions()
            ) { backStackEntry ->
                val viewModel: ManagePromotionViewModel = viewModel(backStackEntry, factory = ManagePromotionViewModel.Factory)
                ManagePromotionScreen(
                    viewModel = viewModel,
                    onBackClick = {navController.popBackStack()},
                )
            }
            composable(
                route = "ListShipper",
                enterTransition = defaultTransitions(),
                exitTransition = defaultExitTransitions(),
                popEnterTransition = defaultPopEnterTransitions(),
                popExitTransition = defaultPopExitTransitions()
            ) { backStackEntry ->
                val viewModel: ListShipperViewModel = viewModel(backStackEntry, factory = ListShipperViewModel.Factory)
                ListShipperScreen(
                    onBackClick = {navController.popBackStack()},
                    onAddShipperClick = {
                        navController.navigate("AddShipper")
                    },
                    onEditShipperClick = { shipperId ->
                        navController.navigate("EditShipper/${shipperId}")
                    },
                    onHomeClick = {
                        navController.navigate("Dashboard") {
                            popUpTo("Dashboard") { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    viewModel = viewModel
                )
            }
            composable(
                route = "AddShipper",
                enterTransition = defaultTransitions(),
                exitTransition = defaultExitTransitions(),
                popEnterTransition = defaultPopEnterTransitions(),
                popExitTransition = defaultPopExitTransitions()
            ) { backStackEntry ->
                val viewModel: ManageShipperViewModel = viewModel(backStackEntry, factory = ManageShipperViewModel.Factory)
                ManageShipperScreen(
                    viewModel = viewModel,
                    onBackClick = {navController.popBackStack()},
                    isAdd = true,
                    isEdit = false,
                )
            }
            composable(
                route = "EditShipper/{shipperId}",
                arguments = listOf(navArgument("shipperId") { type = NavType.LongType }),
                enterTransition = defaultTransitions(),
                exitTransition = defaultExitTransitions(),
                popEnterTransition = defaultPopEnterTransitions(),
                popExitTransition = defaultPopExitTransitions()
            ) { backStackEntry ->
                val viewModel: ManageShipperViewModel = viewModel(backStackEntry, factory = ManageShipperViewModel.Factory)
                ManageShipperScreen(
                    viewModel = viewModel,
                    onBackClick = {navController.popBackStack()},
                    isAdd = false,
                    isEdit = true,
                )
            }
            composable(
                route = "ListUser",
                enterTransition = defaultTransitions(),
                exitTransition = defaultExitTransitions(),
                popEnterTransition = defaultPopEnterTransitions(),
                popExitTransition = defaultPopExitTransitions()
            ) { backStackEntry ->
                val viewModel: ListUserViewModel = viewModel(backStackEntry, factory = ListUserViewModel.Factory)
                ListUserScreen(
                    onBackClick = {navController.popBackStack()},
                    onAddUserClick = {
                        navController.navigate("AddUser")
                    },
                    onEditUserClick = { userId ->
                        navController.navigate("EditUser/${userId}")
                    },
                    onHomeClick = {
                        navController.navigate("Dashboard") {
                            popUpTo("Dashboard") { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onUserClick = { userId ->
                        navController.navigate("DetailsUser/${userId}")
                    },
                    viewModel = viewModel
                )
            }
            composable(
                route = "EditUser/{userId}",
                arguments = listOf(navArgument("userId") { type = NavType.LongType }),
                enterTransition = defaultTransitions(),
                exitTransition = defaultExitTransitions(),
                popEnterTransition = defaultPopEnterTransitions(),
                popExitTransition = defaultPopExitTransitions()
            ) { backStackEntry ->
                val viewModel: ManageUserViewModel = viewModel(backStackEntry, factory = ManageUserViewModel.Factory)
                ManageUserScreen(
                    viewModel = viewModel,
                    onBackClick = {navController.popBackStack()},
                    isEdit = true,
                )
            }
            composable(
                route = "DetailsUser/{userId}",
                arguments = listOf(navArgument("userId") { type = NavType.LongType }),
                enterTransition = defaultTransitions(),
                exitTransition = defaultExitTransitions(),
                popEnterTransition = defaultPopEnterTransitions(),
                popExitTransition = defaultPopExitTransitions()
            ) { backStackEntry ->
                val viewModel: ManageUserViewModel = viewModel(backStackEntry, factory = ManageUserViewModel.Factory)
                ManageUserScreen(
                    viewModel = viewModel,
                    onBackClick = {navController.popBackStack()},
                    isEdit = false,
                )
            }
        }
    }
}
fun defaultTransitions(): AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition? = {
    slideIntoContainer(
        AnimatedContentTransitionScope.SlideDirection.Up,
        tween(500)
    )
}

fun defaultExitTransitions(): AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition? = {
    fadeOut(
        animationSpec = tween(500),
        targetAlpha = 1f
    )
}

fun defaultPopEnterTransitions(): AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition? = {
    fadeIn(
        animationSpec = tween(500),
        initialAlpha = 1f
    )
}

fun defaultPopExitTransitions(): AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition? = {
    slideOutOfContainer(
        AnimatedContentTransitionScope.SlideDirection.Down,
        tween(500)
    )
}