# SangLangThangBank

App Android học tập: luồng mở tài khoản ngân hàng trực tuyến (onboarding). Tên app lấy theo tên chủ dự án (Sang), làm cho vui. Người làm dự án mới học Android. Hãy giải thích bằng tiếng Việt, dễ hiểu, kèm hướng dẫn thao tác trong Android Studio.

## Thông tin kỹ thuật
- Kotlin, Jetpack Compose, Material 3. ViewModel giữ state bằng `mutableStateOf` (`var uiState by mutableStateOf(...)`, `private set`).
- Navigation Compose 2.9.0, route dạng chuỗi, khai báo trong `navigation/AppNavHost.kt`.
- AGP 9.4.1, Gradle 9.6.0, Kotlin 2.2.10, Compose BOM 2025.12.00, minSdk 24, targetSdk/compileSdk 37, Java 11.
- Thư viện khai báo trong `gradle/libs.versions.toml` (version catalog). CameraX 1.6.2 dùng cho camera chụp CCCD.
- Package và applicationId vẫn là `com.example.hydro` (tên từ template, chưa đổi).
- Build: `.\gradlew.bat :app:assembleDebug`. Test: `.\gradlew.bat :app:testDebugUnitTest`.
- Git: remote `https://github.com/sangnt199tb/SangLangThangBank.git`, nhánh `main`.

## Luồng màn hình
Onboarding → OTP → Chụp CCCD → Quét chip NFC → Xác nhận thông tin → Tạo tài khoản đăng nhập → Đăng ký thành công → Màn chính (`HydroApp` trong `MainActivity.kt`, 3 tab mẫu, chưa có nội dung).
- **Onboarding** (`ui/onboarding/`): email, số điện thoại VN (10 số, đầu 03/05/07/08/09), mã MIS (không bắt buộc, 4–10 ký tự chữ/số), captcha 5 ký tự vẽ bằng Canvas, ô tích đồng ý điều khoản (chưa tích thì khóa nút "Tiếp tục"). Gửi thành công thì chuyển sang OTP kèm số điện thoại.
- **OTP** (`ui/otp/`): 6 ô nhập, tự xác nhận khi đủ số, sai tối đa 5 lần, đếm ngược 60 giây mới cho gửi lại mã. Xác thực xong thì xóa màn Onboarding và OTP khỏi back stack.
- **Chụp CCCD** (`ui/idcapture/`): màn tổng quan có 2 ô (mặt trước, mặt sau) và lưu ý khi chụp. Bấm ô thì mở camera trong app (CameraX, `IdCameraScreen`), camera nằm trong cùng route, bật/tắt bằng `cameraSide` trong state. Có khung đúng tỉ lệ thẻ, chụp xong xem lại rồi chọn "Chụp lại" hoặc "Dùng ảnh này". Xong mặt trước thì tự chuyển sang mặt sau. Ảnh được cắt theo khung (hàm thuần `cropBoxInImage` trong `IdCardFrame.kt`) và chỉ giữ trong bộ nhớ, không ghi ra file.
- **Quét chip NFC** (`ui/nfc/`): kiểm tra NFC thật (không hỗ trợ / đang tắt thì có nút "Bật NFC"), các bước IDLE → WAITING (chờ áp thẻ) → READING (%) → SUCCESS/ERROR. Đọc chip qua interface `CccdChipReader` (`data/ekyc/`), hiện dùng `MockCccdChipReader`. Máy không có NFC vẫn cho quét vì đang giả lập. Back thì quay về màn chụp CCCD (ảnh vẫn còn).
- **Xác nhận thông tin** (`ui/confirminfo/`): hiện thông tin đọc từ chip (chỉ xem, không sửa), ô tích xác nhận. Bấm "Xác nhận" thì sang màn tạo tài khoản và xóa các màn eKYC khỏi back stack.
- **Tạo tài khoản đăng nhập** (`ui/register/RegisterScreen.kt`): tên đăng nhập tự điền sẵn số điện thoại, khách sửa được (có nút "Dùng số điện thoại" để quay lại). Quy tắc tên: 6–20 ký tự, chữ thường không dấu, số, `.` `_`, đầu và cuối là chữ/số. Mật khẩu 8–20 ký tự, có chữ hoa, chữ thường, số, ký tự đặc biệt, không chứa tên đăng nhập, hiện danh sách điều kiện tick dần khi gõ. Có ô nhập lại mật khẩu và nút hiện/ẩn. Quy tắc nằm trong `CredentialValidator` (có unit test).
- **Đăng ký thành công** (`ui/register/RegisterSuccessScreen.kt`): hiện tên đăng nhập (truyền qua route), nút "Bắt đầu sử dụng" vào màn chính.
- Dữ liệu mở tài khoản (số điện thoại đã xác thực, dữ liệu chip) chuyển giữa các màn qua `EkycSession` (object trong bộ nhớ), không truyền qua route. OTP xác thực xong thì lưu `phone`. Đăng ký xong thì `EkycSession.clear()`.

## Quy ước code
- Mỗi màn gồm `XxxScreen` (nối với ViewModel) và `XxxContent` private, stateless, có `@Preview`.
- Hàm kiểm tra dữ liệu là hàm thuần (ví dụ `OnboardingValidator`) và có unit test trong `app/src/test`.
- Sự kiện một lần (ví dụ `isSubmitted`) phải được "tiêu thụ" sau khi điều hướng (`onSubmittedHandled()`), để khi quay lại màn không tự chuyển đi lần nữa.
- Chữ trên giao diện và comment viết tiếng Việt, chữ giao diện viết thẳng trong code (chưa dùng `strings.xml`).
- Giao diện: phần đầu màu đỏ dùng `BrandHeader`, thẻ nội dung đè lên phần đầu một đoạn `BrandHeaderOverlap`. Mỗi màn tự gọi `StatusBarIcons(darkIcons = ...)`.
- Màu nằm trong `ui/theme/Color.kt` (đỏ chính `#E11B22`). Đã tắt dynamic color để luôn giữ màu thương hiệu.
- Icon là file vector trong `res/drawable` (không dùng thư viện material-icons-extended).

## Phần đang giả lập (TODO khi có backend)
- Gửi form onboarding, xác thực và gửi lại OTP đang dùng `delay(...)` thay cho gọi API.
- Mã OTP demo cố định là `123456` (hằng `DEMO_OTP`, có hiện trên màn hình).
- Captcha đang tạo và kiểm tra ngay trong app. Bản thật phải do server sinh và kiểm tra.
- Gửi ảnh CCCD 2 mặt (`IdCaptureViewModel.submit`) đang dùng `delay(...)`. Bản thật sẽ tải ảnh lên để server đọc thông tin (OCR).
- Đọc chip dùng `MockCccdChipReader` (chờ 2 giây, luôn thành công, trả về người mẫu `SAMPLE_CITIZEN`). Chưa có ảnh chân dung.
- Gửi thông tin đã xác nhận (`ConfirmInfoViewModel.submit`) đang dùng `delay(...)`.
- Đăng ký (`RegisterViewModel.register`) đang dùng `delay(...)`. Tên đăng nhập đã có người dùng là danh sách giả `DEMO_TAKEN_USERNAMES` (`admin`, `sanglangthang`, `nguyenvanan`).
- Nội dung điều khoản là văn bản mẫu.

## Việc còn dở, cần kiểm tra hoặc xác nhận
- Chưa chạy thử trên điện thoại thật các màn Chụp CCCD, Quét chip, Xác nhận, Tạo tài khoản (mới build và chạy unit test). Cần xem ảnh CCCD cắt theo khung có bị lẹm mép không (nếu có thì chỉnh `margin` trong `cropBoxInImage`).
- Quy tắc tên đăng nhập và mật khẩu trong `CredentialValidator` là do Claude đặt tạm, chờ người dùng hoặc backend xác nhận.
- Đã soạn sẵn 10 câu hỏi gửi đối tác SDK (cách nhận SDK, license, yêu cầu kỹ thuật, dữ liệu cần để mở chip, dữ liệu trả về, xác thực chip, tài liệu, thẻ test, bảo mật, đầu mối hỗ trợ). Chưa rõ người dùng đã gửi cho GTEL chưa.

## Thương hiệu
Logo tự vẽ (3 cột đỏ tăng dần, `ic_brand_mark.xml`) kèm chữ "SangLangThang**Bank**". Không dùng tên, logo hay nhận diện của ngân hàng thật (ví dụ Techcombank). Nếu cần, chỉ dùng màu đỏ – trắng tương tự.

## Việc tiếp theo: tích hợp SDK đọc chip CCCD (NFC) của GTEL
Cách cắm SDK: viết lớp mới implement `CccdChipReader` dùng SDK GTEL, rồi đổi giá trị mặc định `reader` trong `NfcScanViewModel`. Giao diện không cần sửa. Thông tin để mở chip (số CCCD, ngày sinh, ngày hết hạn) sẽ lấy từ kết quả OCR ảnh CCCD hoặc quét MRZ/QR, tùy SDK.

Đang chờ đối tác GTEL gửi:
- File SDK (dự kiến đặt ở `app/libs/`) hoặc thông tin Maven repo.
- Tài liệu và app mẫu (dự kiến đặt ở `docs/sdk/`).
- License được cấp cho package nào: nếu khác `com.example.hydro` thì phải đổi `applicationId`.

Nguyên tắc khi tích hợp:
- Mật khẩu repo và license key không dán vào chat, không commit (repo GitHub có thể đang công khai). Đặt trong file cấu hình cục bộ, ví dụ `local.properties` hoặc `~/.gradle/gradle.properties`.
- Dữ liệu CCCD là dữ liệu cá nhân nhạy cảm (Nghị định 13/2023): không ghi ra log, không lưu lại trên máy.
- Phải test trên điện thoại thật có NFC, máy ảo không đọc được chip.
