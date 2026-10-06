# 🏆 GMAchivement - Plugin Thành Tựu Máy Chủ

**GMAchivement** là một plugin quản lý thành tựu mạnh mẽ, tối ưu hiệu năng và dễ dàng cấu hình dành cho server Minecraft. Plugin hỗ trợ chia nhỏ các file cấu hình giúp bạn dễ dàng quản lý hàng nghìn nhiệm vụ khác nhau, đồng thời hỗ trợ móc nối (hook) vào các plugin phổ biến như MythicMobs, ExcellentCrates và PlayerPoints.

---

## 🌟 Tính Năng Nổi Bật

- **Không Memory Leak:** Kiến trúc quản lý bộ nhớ thông minh, tự động giải phóng cache khi người chơi thoát game.
- **Không Bug Kéo Đồ:** Giao diện nhận thưởng (GUI) được khóa chặt bằng `InventoryClickEvent` và `InventoryDragEvent`, chống hoàn toàn việc lấy cắp item hay lợi dụng lỗi.
- **Tùy Biến Đỉnh Cao:** Không bắt buộc gom mọi thứ vào 1 file! Có thể tạo bao nhiêu file tùy thích trong thư mục `achievements/`.
- **Đa Dạng Loại Nhiệm Vụ (Types):** Hỗ trợ tới 12 loại sự kiện tương tác khác nhau trong game.

---

## 📂 Cấu Trúc Thư Mục

Sau khi chạy plugin, thư mục `GMAchivement` sẽ được tạo ra với cấu trúc sau:
```text
GMAchivement/
├── data/                 # Chứa dữ liệu cá nhân của người chơi (Tự động sinh)
├── achievements/         # THƯ MỤC CHÍNH: Chứa các file thành tựu (.yml)
│   ├── combat.yml        # (Ví dụ: Thành tựu đánh nhau)
│   ├── mining.yml        # (Ví dụ: Thành tựu đào khoáng sản)
│   └── ...               # Bạn có thể tạo thêm tùy ý!
├── categories.yml        # Cấu hình danh mục hiển thị trên GUI chính
├── config.yml            # Cấu hình chung của plugin
└── messages.yml          # Việt hóa tin nhắn và tên các Type
```

---

## 🎮 Lệnh & Phân Quyền

| Lệnh | Quyền (Permission) | Mô tả |
| :--- | :--- | :--- |
| `/thanhtuu` (hoặc `/tt`) | `gmachivement.use` | Mở giao diện thành tựu cá nhân. (Mặc định ai cũng dùng được) |
| `/thanhtuu reload` | `gmachivement.admin` | Tải lại toàn bộ cấu hình plugin mà không cần khởi động lại server. (Chỉ dành cho Admin) |

---

## 📖 Hướng Dẫn Cấu Hình (Wiki)

### 1. Thêm một Danh Mục (Category) Mới
Mở file `categories.yml`. Mỗi danh mục đại diện cho 1 nút bấm trên Giao Diện Chính.

```yaml
categories:
  farming:                   # ID của danh mục (Dùng để liên kết với thành tựu)
    display-name: "&6🌾 Nông Nghiệp" # Tên hiển thị
    icon: WHEAT              # Vật phẩm đại diện (chuẩn Material Bukkit)
    slot: 16                 # Vị trí trên giao diện (từ 0 đến 53)
    lore:                    # Dòng mô tả (Lore)
      - "&7Các thành tựu liên quan đến trồng trọt."
```

### 2. Tạo Một Thành Tựu Mới
Bạn có thể tạo một file bất kỳ (VD: `nong-trai.yml`) bỏ vào thư mục `achievements/`. Tất cả các file trong thư mục này đều được plugin tự động load.

**Cấu trúc 1 thành tựu chuẩn:**
```yaml
achievements:
  lua_mi_dai_nhan:                  # ID duy nhất của thành tựu này
    category: "farming"             # BẮT BUỘC: ID của danh mục chứa nó (như ở bước 1)
    display-name: "&eĐại Nhân Lúa Mì"
    description: "&7Thu hoạch 1,000 cây lúa."
    type: BLOCK_BREAK               # BẮT BUỘC: Loại nhiệm vụ (Xem bảng bên dưới)
    target-filter: "WHEAT"          # BẮT BUỘC: Mục tiêu cụ thể. (Nếu type không cần, hãy bỏ trống)
    target: 1000                    # BẮT BUỘC: Số lần thực hiện để nhận thưởng
    rewards:                        # Danh sách lệnh phát thưởng chạy từ Console
      - "eco give %player% 50000"
      - "tcsendmsg %player% &aBạn đã hoàn thành nhiệm vụ!"
```

---

## 🛠 Bảng Danh Sách Loại Nhiệm Vụ (Types) Hỗ Trợ

| Tên Type | Tác Dụng | Ghi chú `target-filter` |
| :--- | :--- | :--- |
| `BLOCK_BREAK` | Đập/Phá khối | Điền ID Block (vd: `STONE`, `WHEAT`) |
| `BLOCK_PLACE` | Đặt khối / Trồng cây | Điền ID Block (vd: `OAK_SAPLING`) |
| `ENTITY_KILL` | Giết quái vật thường | Điền ID Quái (vd: `ZOMBIE`, `SKELETON`) |
| `PLAYER_KILL` | Giết người chơi khác | Điền `"PLAYER"` |
| `MYTHIC_MOB_KILL` | Giết Boss MythicMobs | Điền Internal Name của Boss (vd: `SkeletonKing`) |
| `EXCELLENT_CRATE_OPEN` | Mở rương ExcellentCrates | Điền ID của rương (vd: `epic`, `normal`) |
| `PLAY_TIME` | Online tích lũy (Tính bằng phút) | Bỏ trống `""` |
| `CHAT_MESSAGE` | Số tin nhắn chat đã gửi | Bỏ trống `""` |
| `COMMAND_EXECUTE` | Dùng một lệnh cụ thể | Điền lệnh (vd: `/spawn`, `/home`) hoặc bỏ trống để tính mọi lệnh |
| `PLAYER_JOIN` | Đăng nhập vào server | Bỏ trống `""` |
| `PLAYER_POINTS_EARN` | Nhận / Kiếm PlayerPoints | Bỏ trống `""` |
| `PLAYER_POINTS_SPEND` | Tiêu PlayerPoints | Bỏ trống `""` |
| `CUSTOM` | Dùng cho API từ các plugin khác | Tùy chọn |

---
*Cảm ơn bạn đã sử dụng GMAchivement! 🐢 Chúc máy chủ của bạn phát triển mạnh mẽ!*
