import yaml
import os

mining_achievements = {"achievements": {}}

# Categories and targets for mining (50 achievements total)
mining_data = [
    ("stone", "Đá", "STONE", [1000, 5000, 10000, 50000, 100000], "BLOCK_BREAK", 500),
    ("coal", "Than", "COAL_ORE", [100, 500, 1000, 5000, 10000], "BLOCK_BREAK", 500),
    ("deepslate_coal", "Than Đá Đen", "DEEPSLATE_COAL_ORE", [100, 500, 1000, 5000], "BLOCK_BREAK", 600),
    ("iron", "Sắt", "IRON_ORE", [100, 500, 1000, 5000], "BLOCK_BREAK", 1000),
    ("deepslate_iron", "Sắt Đá Đen", "DEEPSLATE_IRON_ORE", [100, 500, 1000, 5000], "BLOCK_BREAK", 1200),
    ("gold", "Vàng", "GOLD_ORE", [50, 200, 1000, 5000], "BLOCK_BREAK", 2000),
    ("deepslate_gold", "Vàng Đá Đen", "DEEPSLATE_GOLD_ORE", [50, 200, 1000, 5000], "BLOCK_BREAK", 2500),
    ("redstone", "Đá Đỏ", "REDSTONE_ORE", [100, 500, 2000, 10000], "BLOCK_BREAK", 1500),
    ("lapis", "Lưu Ly", "LAPIS_ORE", [100, 500, 2000, 10000], "BLOCK_BREAK", 1500),
    ("diamond", "Kim Cương", "DIAMOND_ORE", [10, 50, 200, 1000], "BLOCK_BREAK", 5000),
    ("deepslate_diamond", "Kim Cương Đá Đen", "DEEPSLATE_DIAMOND_ORE", [10, 50, 200, 1000], "BLOCK_BREAK", 6000),
    ("emerald", "Ngọc Lục Bảo", "EMERALD_ORE", [5, 20, 100], "BLOCK_BREAK", 10000),
    ("netherrack", "Đá Địa Ngục", "NETHERRACK", [1000, 5000, 10000], "BLOCK_BREAK", 300),
    ("quartz", "Thạch Anh", "NETHER_QUARTZ_ORE", [100, 500, 2000], "BLOCK_BREAK", 1000),
    ("ancient_debris", "Mảnh Vỡ Cổ Đại", "ANCIENT_DEBRIS", [5, 20, 100], "BLOCK_BREAK", 20000),
    ("obsidian", "Hắc Diện Thạch", "OBSIDIAN", [50, 200, 1000], "BLOCK_BREAK", 3000),
]

count = 0
for id_prefix, name_prefix, target_filter, targets, ach_type, base_reward in mining_data:
    for i, target in enumerate(targets):
        if count >= 50:
            break
        level = i + 1
        tier_names = ["Tân Binh", "Thành Thạo", "Chuyên Gia", "Bậc Thầy", "Huyền Thoại"]
        tier_name = tier_names[i] if i < len(tier_names) else f"Cấp {level}"
        
        ach_id = f"miner_{id_prefix}_{level}"
        display_name = f"&bThợ Mỏ {name_prefix} {tier_name}"
        reward_coins = base_reward * target
        
        # Scale reward coins to not be overly insane but good enough
        reward_coins = int(base_reward * (level * 1.5))

        ach = {
            "category": "mining",
            "display-name": display_name,
            "lore": [
                f"&7Đào {target} khối {name_prefix}.",
                "",
                "&f🎁 &aPhần thưởng:",
                "  &8▪ &bĐã giấu kín (hoàn thành để nhận)"
            ],
            "type": ach_type,
            "target-filter": target_filter,
            "target": target,
            "rewards": [
                f"eco give %player% {reward_coins}"
            ]
        }
        mining_achievements["achievements"][ach_id] = ach
        count += 1

with open(r'd:\.gemini\antigravity\scratch\GMAchivement\GMAchivement-plugin\src\main\resources\achievements\mining.yml', 'w', encoding='utf-8') as f:
    yaml.dump(mining_achievements, f, allow_unicode=True, sort_keys=False, default_flow_style=False)

print(f"Generated {count} mining achievements")
