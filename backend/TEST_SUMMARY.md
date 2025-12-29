# Unit Test Summary

This document provides a comprehensive summary of all unit tests in the test folder of the SE114.P22.Project backend.

---

## Table of Contents
1. [BackendApplicationTests](#1-backendapplicationtests)
2. [Controller Tests](#2-controller-tests)
3. [Service Tests](#3-service-tests)

---

## 1. BackendApplicationTests

| No. | Test Class | Target Class | Function Name |
|-----|------------|--------------|---------------|
| 1 | BackendApplicationTests | BackendApplication | contextLoads |

---

## 2. Controller Tests

### 2.1 AuthControllerTest

| No. | Test Class | Target Class | Function Name |
|-----|------------|--------------|---------------|
| 1 | AuthControllerTest | AuthController | register_returns201 |
| 2 | AuthControllerTest | AuthController | login_returns200 |

---

### 2.2 CartControllerTest

| No. | Test Class | Target Class | Function Name |
|-----|------------|--------------|---------------|
| 1 | CartControllerTest | CartController | getMyCart_returns200 |
| 2 | CartControllerTest | CartController | getCartById_returns404_whenNotExists |
| 3 | CartControllerTest | CartController | getCartById_returns200_whenExists |
| 4 | CartControllerTest | CartController | createNewCart_returns200 |
| 5 | CartControllerTest | CartController | deleteCart_returns404_whenNotBelongs |
| 6 | CartControllerTest | CartController | deleteCart_returns204_whenDeleted |
| 7 | CartControllerTest | CartController | countMyCartItems_returns200 |

---

### 2.3 OrderControllerTest

| No. | Test Class | Target Class | Function Name |
|-----|------------|--------------|---------------|
| 1 | OrderControllerTest | OrderController | findById_returns200 |
| 2 | OrderControllerTest | OrderController | getMyOrders_returns200 |
| 3 | OrderControllerTest | OrderController | getAllOrders_returns200 |
| 4 | OrderControllerTest | OrderController | createOrder_returns200 |
| 5 | OrderControllerTest | OrderController | updateOrder_returns200 |
| 6 | OrderControllerTest | OrderController | deleteOrder_returns204 |
| 7 | OrderControllerTest | OrderController | cancelOrder_returns204 |
| 8 | OrderControllerTest | OrderController | markDelivered_returns200 |
| 9 | OrderControllerTest | OrderController | updateStatus_returns204 |

---

### 2.4 ProductControllerTest

| No. | Test Class | Target Class | Function Name |
|-----|------------|--------------|---------------|
| 1 | ProductControllerTest | ProductController | getAllProducts_returns200 |
| 2 | ProductControllerTest | ProductController | getProductsByCategory_returns200 |
| 3 | ProductControllerTest | ProductController | getProductById_returns200 |
| 4 | ProductControllerTest | ProductController | createProduct_multipart_returns200 |
| 5 | ProductControllerTest | ProductController | updateProduct_multipart_returns200 |
| 6 | ProductControllerTest | ProductController | deleteProduct_returns204 |
| 7 | ProductControllerTest | ProductController | getRecommendedProducts_returns200 |

---

### 2.5 UserControllerTest

| No. | Test Class | Target Class | Function Name |
|-----|------------|--------------|---------------|
| 1 | UserControllerTest | UserController | getAllUsers_paged_forwardToService |
| 2 | UserControllerTest | UserController | getAllUsers_unpaged_usesUnpaged |
| 3 | UserControllerTest | UserController | getUserById_success |
| 4 | UserControllerTest | UserController | updateUser_callsService |
| 5 | UserControllerTest | UserController | deleteUser_callsService |
| 6 | UserControllerTest | UserController | assignRoleToUser_callsService |
| 7 | UserControllerTest | UserController | updateStatus_success |
| 8 | UserControllerTest | UserController | updateStatus_invalid_throws |

---

### 2.6 UserControllerIntegrationTest

| No. | Test Class | Target Class | Function Name |
|-----|------------|--------------|---------------|
| 1 | UserControllerIntegrationTest.GetAllUsersTests | UserController | getAllUsers_Unauthenticated_ReturnsUnauthorized |
| 2 | UserControllerIntegrationTest.GetAllUsersTests | UserController | getAllUsers_Authenticated_ReturnsPaginatedList |
| 3 | UserControllerIntegrationTest.GetAllUsersTests | UserController | getAllUsers_Unpaged_ReturnsAllUsers |
| 4 | UserControllerIntegrationTest.GetAllUsersTests | UserController | getAllUsers_WithFilter_ReturnsFilteredUsers |
| 5 | UserControllerIntegrationTest.GetAllUsersTests | UserController | getAllUsers_NoMatch_ReturnsEmptyPage |
| 6 | UserControllerIntegrationTest.GetUserByIdTests | UserController | getUserById_Unauthenticated_ReturnsUnauthorized |
| 7 | UserControllerIntegrationTest.GetUserByIdTests | UserController | getUserById_Found_ReturnsUser |
| 8 | UserControllerIntegrationTest.GetUserByIdTests | UserController | getUserById_NotFound_Returns404 |
| 9 | UserControllerIntegrationTest.GetUserByIdTests | UserController | getUserById_InvalidId_ReturnsBadRequest |
| 10 | UserControllerIntegrationTest.UpdateUserTests | UserController | updateUser_Unauthenticated_ReturnsUnauthorized |
| 11 | UserControllerIntegrationTest.UpdateUserTests | UserController | updateUser_ValidRequest_ReturnsUpdatedUser |
| 12 | UserControllerIntegrationTest.UpdateUserTests | UserController | updateUser_WithAvatar_ReturnsUpdatedUser |
| 13 | UserControllerIntegrationTest.UpdateUserTests | UserController | updateUser_NotFound_Returns404 |
| 14 | UserControllerIntegrationTest.UpdateUserTests | UserController | updateUser_BlankFullname_ReturnsBadRequest |
| 15 | UserControllerIntegrationTest.UpdateUserTests | UserController | updateUser_InvalidUsername_ReturnsBadRequest |
| 16 | UserControllerIntegrationTest.UpdateUserTests | UserController | updateUser_InvalidEmail_ReturnsBadRequest |
| 17 | UserControllerIntegrationTest.UpdateUserTests | UserController | updateUser_InvalidPhone_ReturnsBadRequest |
| 18 | UserControllerIntegrationTest.UpdateUserTests | UserController | updateUser_NullRoleId_ReturnsBadRequest |
| 19 | UserControllerIntegrationTest.DeleteUserTests | UserController | deleteUser_Unauthenticated_ReturnsUnauthorized |
| 20 | UserControllerIntegrationTest.DeleteUserTests | UserController | deleteUser_ValidId_ReturnsNoContent |
| 21 | UserControllerIntegrationTest.DeleteUserTests | UserController | deleteUser_NotFound_Returns404 |
| 22 | UserControllerIntegrationTest.DeleteUserTests | UserController | deleteUser_InvalidId_ReturnsBadRequest |
| 23 | UserControllerIntegrationTest.AssignRoleToUserTests | UserController | assignRole_Unauthenticated_ReturnsUnauthorized |
| 24 | UserControllerIntegrationTest.AssignRoleToUserTests | UserController | assignRole_NonAdmin_ReturnsForbidden |
| 25 | UserControllerIntegrationTest.AssignRoleToUserTests | UserController | assignRole_AsAdmin_ReturnsNoContent |
| 26 | UserControllerIntegrationTest.AssignRoleToUserTests | UserController | assignRole_UserNotFound_Returns404 |
| 27 | UserControllerIntegrationTest.AssignRoleToUserTests | UserController | assignRole_RoleNotFound_Returns404 |
| 28 | UserControllerIntegrationTest.UpdateUserStatusTests | UserController | updateStatus_Unauthenticated_ReturnsUnauthorized |
| 29 | UserControllerIntegrationTest.UpdateUserStatusTests | UserController | updateStatus_NonAdmin_ReturnsError |
| 30 | UserControllerIntegrationTest.UpdateUserStatusTests | UserController | updateStatus_ToInactive_ReturnsNoContent |
| 31 | UserControllerIntegrationTest.UpdateUserStatusTests | UserController | updateStatus_ToBlocked_ReturnsNoContent |
| 32 | UserControllerIntegrationTest.UpdateUserStatusTests | UserController | updateStatus_ToPending_ReturnsNoContent |
| 33 | UserControllerIntegrationTest.UpdateUserStatusTests | UserController | updateStatus_ToDeleted_ReturnsNoContent |
| 34 | UserControllerIntegrationTest.UpdateUserStatusTests | UserController | updateStatus_InvalidStatus_ReturnsBadRequest |
| 35 | UserControllerIntegrationTest.UpdateUserStatusTests | UserController | updateStatus_UserNotFound_Returns404 |
| 36 | UserControllerIntegrationTest.UpdateUserStatusTests | UserController | updateStatus_MissingStatus_ReturnsBadRequest |
| 37 | UserControllerIntegrationTest.ResponseDtoValidationTests | UserController | getUserById_CorrectResponseStructure |
| 38 | UserControllerIntegrationTest.ResponseDtoValidationTests | UserController | getAllUsers_CorrectPageStructure |
| 39 | UserControllerIntegrationTest.EdgeCasesTests | UserController | getAllUsers_LargePageNumber_ReturnsEmptyPage |
| 40 | UserControllerIntegrationTest.EdgeCasesTests | UserController | getAllUsers_SmallPageSize_ReturnsSingleItem |
| 41 | UserControllerIntegrationTest.EdgeCasesTests | UserController | getAllUsers_SortByUsername_ReturnsSortedList |
| 42 | UserControllerIntegrationTest.EdgeCasesTests | UserController | getAllUsers_FilterByStatus_ReturnsFilteredUsers |

---

### 2.7 VariationControllerTest

| No. | Test Class | Target Class | Function Name |
|-----|------------|--------------|---------------|
| 1 | VariationControllerTest | VariationController | getVariationsByProduct_returns200_withFilters |
| 2 | VariationControllerTest | VariationController | getVariationsByProduct_returns200_withoutName |
| 3 | VariationControllerTest | VariationController | createVariation_returns200 |
| 4 | VariationControllerTest | VariationController | updateVariation_returns200 |
| 5 | VariationControllerTest | VariationController | deleteVariation_returns204 |

---

### 2.8 VariationOptionControllerTest

| No. | Test Class | Target Class | Function Name |
|-----|------------|--------------|---------------|
| 1 | VariationOptionControllerTest | VariationOptionController | getByVariationId_returns200_withPageable |
| 2 | VariationOptionControllerTest | VariationOptionController | getByVariationId_returns200_unpaged |
| 3 | VariationOptionControllerTest | VariationOptionController | create_returns200 |
| 4 | VariationOptionControllerTest | VariationOptionController | update_returns200 |
| 5 | VariationOptionControllerTest | VariationOptionController | delete_returns204 |

---

## 3. Service Tests

### 3.1 AuthServiceImplTest

| No. | Test Class | Target Class | Function Name |
|-----|------------|--------------|---------------|
| 1 | AuthServiceImplTest | AuthServiceImpl | login_returnsTwoFAChallenge_when2FAEnabled |
| 2 | AuthServiceImplTest | AuthServiceImpl | login_returnsAuthResponse_when2FADisabled |

---

### 3.2 CartItemServiceImplTest

| No. | Test Class | Target Class | Function Name |
|-----|------------|--------------|---------------|
| 1 | CartItemServiceImplTest | CartItemServiceImpl | getAllCartItems_mapsPage |
| 2 | CartItemServiceImplTest | CartItemServiceImpl | createCartItem_createsCartAndCalculatesPrice |
| 3 | CartItemServiceImplTest | CartItemServiceImpl | createCartItem_mergesExisting |
| 4 | CartItemServiceImplTest | CartItemServiceImpl | updateCartItem_cases |
| 5 | CartItemServiceImplTest | CartItemServiceImpl | deleteCartItem_delegates |

---

### 3.3 CartServiceImplTest

| No. | Test Class | Target Class | Function Name |
|-----|------------|--------------|---------------|
| 1 | CartServiceImplTest | CartServiceImpl | getCartResponseByUserId_success |
| 2 | CartServiceImplTest | CartServiceImpl | getCartResponseByUserId_notFound |
| 3 | CartServiceImplTest | CartServiceImpl | getCartById_success |
| 4 | CartServiceImplTest | CartServiceImpl | getCartById_notFound |
| 5 | CartServiceImplTest | CartServiceImpl | create_success |
| 6 | CartServiceImplTest | CartServiceImpl | create_conflict |
| 7 | CartServiceImplTest | CartServiceImpl | delete_success |
| 8 | CartServiceImplTest | CartServiceImpl | delete_notFound |
| 9 | CartServiceImplTest | CartServiceImpl | existsByIdAndUserId |
| 10 | CartServiceImplTest | CartServiceImpl | countCartItemsByUserId_cases |

---

### 3.4 OrderServiceImplTest

| No. | Test Class | Target Class | Function Name |
|-----|------------|--------------|---------------|
| 1 | OrderServiceImplTest | OrderServiceImpl | getAll_success |
| 2 | OrderServiceImplTest | OrderServiceImpl | getById_cases |
| 3 | OrderServiceImplTest | OrderServiceImpl | getOrdersByUserId_success |
| 4 | OrderServiceImplTest | OrderServiceImpl | create_success_noPromotion |
| 5 | OrderServiceImplTest | OrderServiceImpl | create_withPromotion_appliesDiscount |
| 6 | OrderServiceImplTest | OrderServiceImpl | create_error_cases |
| 7 | OrderServiceImplTest | OrderServiceImpl | update_cases |
| 8 | OrderServiceImplTest | OrderServiceImpl | cancelOrder_cases |
| 9 | OrderServiceImplTest | OrderServiceImpl | delete_cases |
| 10 | OrderServiceImplTest | OrderServiceImpl | markOrderAsDelivered_cases |
| 11 | OrderServiceImplTest | OrderServiceImpl | updateStatus_cases |
| 12 | OrderServiceImplTest | OrderServiceImpl | markPayment_cases |

---

### 3.5 ProductServiceImplTest

| No. | Test Class | Target Class | Function Name |
|-----|------------|--------------|---------------|
| 1 | ProductServiceImplTest | ProductServiceImpl | getAllProducts_success |
| 2 | ProductServiceImplTest | ProductServiceImpl | getProductsByCategory_success |
| 3 | ProductServiceImplTest | ProductServiceImpl | create_withImage_success |
| 4 | ProductServiceImplTest | ProductServiceImpl | create_withoutImage_success |
| 5 | ProductServiceImplTest | ProductServiceImpl | create_categoryNotFound |
| 6 | ProductServiceImplTest | ProductServiceImpl | update_productNotFound |
| 7 | ProductServiceImplTest | ProductServiceImpl | update_changeCategoryAndImage |
| 8 | ProductServiceImplTest | ProductServiceImpl | update_keepExisting_noCategoryChange_noImage |
| 9 | ProductServiceImplTest | ProductServiceImpl | delete_notFound |
| 10 | ProductServiceImplTest | ProductServiceImpl | delete_alreadyDeleted |
| 11 | ProductServiceImplTest | ProductServiceImpl | delete_success |
| 12 | ProductServiceImplTest | ProductServiceImpl | getProductById_success |
| 13 | ProductServiceImplTest | ProductServiceImpl | getProductById_notFound |
| 14 | ProductServiceImplTest | ProductServiceImpl | getRecommendedProducts_enough |
| 15 | ProductServiceImplTest | ProductServiceImpl | getRecommendedProducts_fallback |

---

### 3.6 UserServiceImplTest

| No. | Test Class | Target Class | Function Name |
|-----|------------|--------------|---------------|
| 1 | UserServiceImplTest | UserServiceImpl | getAllUsers_returnsMappedPage |
| 2 | UserServiceImplTest | UserServiceImpl | getUserById_returnsUser |
| 3 | UserServiceImplTest | UserServiceImpl | getUserById_throwsNotFound |
| 4 | UserServiceImplTest | UserServiceImpl | findByPhone_returnsUser |
| 5 | UserServiceImplTest | UserServiceImpl | findByPhone_throws |
| 6 | UserServiceImplTest | UserServiceImpl | register_success |
| 7 | UserServiceImplTest | UserServiceImpl | register_conflict_throws |
| 8 | UserServiceImplTest | UserServiceImpl | register_roleMissing_throws |
| 9 | UserServiceImplTest | UserServiceImpl | update_savesAndStoresAvatar |
| 10 | UserServiceImplTest | UserServiceImpl | update_avatarEmpty_noStore |
| 11 | UserServiceImplTest | UserServiceImpl | delete_deletesAndRemovesAvatar |
| 12 | UserServiceImplTest | UserServiceImpl | delete_noAvatar |
| 13 | UserServiceImplTest | UserServiceImpl | registerGoogleUser_savesNewUser |
| 14 | UserServiceImplTest | UserServiceImpl | registerFirebaseUser_invalidToken_throws |
| 15 | UserServiceImplTest | UserServiceImpl | registerFirebaseUser_valid_saves |
| 16 | UserServiceImplTest | UserServiceImpl | assignRoleToUser_success |
| 17 | UserServiceImplTest | UserServiceImpl | assignRoleToUser_missing_throws |
| 18 | UserServiceImplTest | UserServiceImpl | updateUserStatus_setsAndSaves |
| 19 | UserServiceImplTest | UserServiceImpl | getCurrentUser_notAuthenticated_throws |
| 20 | UserServiceImplTest | UserServiceImpl | getCurrentUser_returns |

---

### 3.7 VariationServiceImplTest

| No. | Test Class | Target Class | Function Name |
|-----|------------|--------------|---------------|
| 1 | VariationServiceImplTest | VariationServiceImpl | getVariationsByProductId_withoutNameFilter_success |
| 2 | VariationServiceImplTest | VariationServiceImpl | getVariationsByProductId_withNameFilter_success |
| 3 | VariationServiceImplTest | VariationServiceImpl | getVariationsByProductId_noVariationsFound_returnsEmptyPage |
| 4 | VariationServiceImplTest | VariationServiceImpl | getVariationsByProductId_withBlankNameFilter_success |
| 5 | VariationServiceImplTest | VariationServiceImpl | create_productExists_success |
| 6 | VariationServiceImplTest | VariationServiceImpl | create_productNotFound_throwsException |
| 7 | VariationServiceImplTest | VariationServiceImpl | create_isMultipleChoiceFalse_success |
| 8 | VariationServiceImplTest | VariationServiceImpl | update_withNewProduct_success |
| 9 | VariationServiceImplTest | VariationServiceImpl | update_withoutNewProduct_success |
| 10 | VariationServiceImplTest | VariationServiceImpl | update_variationNotFound_throwsException |
| 11 | VariationServiceImplTest | VariationServiceImpl | update_newProductNotFound_throwsException |
| 12 | VariationServiceImplTest | VariationServiceImpl | update_updateNameOnly_success |
| 13 | VariationServiceImplTest | VariationServiceImpl | delete_variationExists_success |
| 14 | VariationServiceImplTest | VariationServiceImpl | delete_variationNotFound_throwsException |
| 15 | VariationServiceImplTest | VariationServiceImpl | delete_variationIdZero_success |

---

### 3.8 VariationOptionServiceImplTest

| No. | Test Class | Target Class | Function Name |
|-----|------------|--------------|---------------|
| 1 | VariationOptionServiceImplTest | VariationOptionServiceImpl | getByVariationId_success |
| 2 | VariationOptionServiceImplTest | VariationOptionServiceImpl | getByVariationId_noOptionsFound_returnsEmptyPage |
| 3 | VariationOptionServiceImplTest | VariationOptionServiceImpl | getByVariationId_largePage_success |
| 4 | VariationOptionServiceImplTest | VariationOptionServiceImpl | create_variationExistsAndValueUnique_success |
| 5 | VariationOptionServiceImplTest | VariationOptionServiceImpl | create_variationNotFound_throwsException |
| 6 | VariationOptionServiceImplTest | VariationOptionServiceImpl | create_duplicateValue_noExceptionThrown |
| 7 | VariationOptionServiceImplTest | VariationOptionServiceImpl | create_zeroAdditionalPrice_success |
| 8 | VariationOptionServiceImplTest | VariationOptionServiceImpl | update_withNewValue_success |
| 9 | VariationOptionServiceImplTest | VariationOptionServiceImpl | update_withSameValue_success |
| 10 | VariationOptionServiceImplTest | VariationOptionServiceImpl | update_optionNotFound_throwsException |
| 11 | VariationOptionServiceImplTest | VariationOptionServiceImpl | update_duplicateValue_throwsException |
| 12 | VariationOptionServiceImplTest | VariationOptionServiceImpl | update_variationNotFound_throwsException |
| 13 | VariationOptionServiceImplTest | VariationOptionServiceImpl | update_marksCartItemsUnavailable |
| 14 | VariationOptionServiceImplTest | VariationOptionServiceImpl | update_noCartItems_success |
| 15 | VariationOptionServiceImplTest | VariationOptionServiceImpl | delete_success |
| 16 | VariationOptionServiceImplTest | VariationOptionServiceImpl | delete_optionNotFound_throwsException |
| 17 | VariationOptionServiceImplTest | VariationOptionServiceImpl | delete_noCartItems_success |
| 18 | VariationOptionServiceImplTest | VariationOptionServiceImpl | delete_multipleCartItems_marksAllUnavailable |
| 19 | VariationOptionServiceImplTest | VariationOptionServiceImpl | delete_savesBeforeDeleting |

---

## Summary Statistics

| Category | Count |
|----------|-------|
| **Total Test Classes** | 17 |
| **Total Test Methods** | 147 |
| **Controller Tests** | 77 |
| **Service Tests** | 69 |
| **Application Tests** | 1 |

### Breakdown by Module

| Module | Test Classes | Test Methods |
|--------|--------------|--------------|
| Authentication | 2 | 4 |
| Cart | 3 | 17 |
| Order | 2 | 21 |
| Product | 2 | 22 |
| User | 3 | 70 |
| Variation | 4 | 39 |
| Application | 1 | 1 |

---

*Generated on December 29, 2025*

