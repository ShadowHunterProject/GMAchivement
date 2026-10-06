import yaml
import os

base_dir = r'd:\.gemini\antigravity\scratch\GMAchivement\GMAchivement-plugin\src\main\resources\achievements'

def save_yaml(filename, data):
    filepath = os.path.join(base_dir, filename)
    with open(filepath, 'w', encoding='utf-8') as f:
        yaml.dump(data, f, allow_unicode=True, sort_keys=False, default_flow_style=False)

def get_tier_name(level):
    tiers = ["Tân Binh", "Thành Thạo", "Chuyên Gia", "Bậc Thầy", "Huyền Thoại", "Thần Thoại", "Chí Tôn", "Vô Cực"]
    if level <= len(tiers):
        return tiers[level - 1]
    return f"Cấp {level}"

def calculate_reward(base, level, is_scaling_fast=False):
    if is_scaling_fast:
        amount = base + (level * level * 800)
    else:
        amount = base + (level * 1500)
    return min(30000, max(100, int(amount)))

def format_reward_lore(rewards):
    lore = []
    for r in rewards:
        if r.startswith("sololeveling forceattr %player% vitality "):
            amount = int(r.split()[-1])
            lore.append(f"  &8▪ &c+{amount} Điểm chỉ số Máu")
        elif r.startswith("eco give %player% "):
            amount = int(r.split()[-1])
            lore.append(f"  &8▪ &b{amount:,} Tiền")
        elif r.startswith("give %player% diamond_block "):
            amount = int(r.split()[-1])
            lore.append(f"  &8▪ &b{amount}x Khối Kim Cương")
        elif r.startswith("give %player% emerald_block "):
            amount = int(r.split()[-1])
            lore.append(f"  &8▪ &b{amount}x Khối Ngọc Lục Bảo")
    return lore

def generate_generic_targets(start, end, levels=50):
    targets = []
    for i in range(levels):
        if i == 0:
            val = start
        elif i == levels - 1:
            val = end
        else:
            raw = start + (end - start) * (i / float(levels - 1))
            if end - start > 1000:
                val = int(round(raw / 10.0) * 10)
            else:
                val = int(round(raw))
        targets.append(val)
    return targets

# 1. PLAYER_KILL
def gen_player_kill():
    achievements = {"achievements": {}}
    targets = generate_generic_targets(20, 10000)
    for i in range(50):
        level = i + 1
        target = targets[i]
        hp_reward = int(round(1 + i * (19.0 / 49)))
        rewards_list = [f"sololeveling forceattr %player% vitality {hp_reward}"]
        lore = [f"#AAAAAA▶ #FFFFFFNhiệm vụ: #FF1A1ATiêu diệt {target:,} người chơi.", "", "#FFD700✦ Phần thưởng vinh dự:"] + format_reward_lore(rewards_list)
        achievements["achievements"][f"pvp_killer_{level}"] = {
            "category": "player_kill",
            "display-name": f"#FF1A1A⚔ Sát Nhân #FFDD33★ {get_tier_name(level)}",
            "lore": lore,
            "type": "PLAYER_KILL",
            "target": target,
            "rewards": rewards_list
        }
    save_yaml('player_kill.yml', achievements)

# 2. ENTITY_KILL
def gen_entity_kill():
    achievements = {"achievements": {}}
    mob_data = [
        ("zombie", "Zombie", "ZOMBIE", [50, 200, 1000, 5000, 10000]),
        ("skeleton", "Skeleton", "SKELETON", [50, 200, 1000, 5000, 10000]),
        ("spider", "Nhện", "SPIDER", [50, 200, 1000, 5000]),
        ("creeper", "Creeper", "CREEPER", [50, 200, 1000, 5000]),
        ("enderman", "Enderman", "ENDERMAN", [20, 100, 500, 2000]),
        ("blaze", "Blaze", "BLAZE", [20, 100, 500, 2000]),
        ("wither_skeleton", "Wither Skeleton", "WITHER_SKELETON", [20, 100, 500]),
        ("slime", "Slime", "SLIME", [50, 200, 1000]),
        ("magma_cube", "Magma Cube", "MAGMA_CUBE", [50, 200, 1000]),
        ("witch", "Phù Thủy", "WITCH", [10, 50, 200]),
        ("phantom", "Phantom", "PHANTOM", [10, 50, 200]),
        ("piglin", "Piglin", "PIGLIN", [50, 200, 1000]),
    ]
    count = 0
    for id_prefix, name_prefix, target_filter, targets in mob_data:
        for i, target in enumerate(targets):
            if count >= 50: break
            level = i + 1
            rewards_list = [f"eco give %player% {calculate_reward(500, level)}"]
            lore = [f"&7Tiêu diệt {target} {name_prefix}.", "", "&f🎁 &aPhần thưởng:"] + format_reward_lore(rewards_list)
            achievements["achievements"][f"hunter_{id_prefix}_{level}"] = {
                "category": "entity_kill",
                "display-name": f"&eThợ Săn {name_prefix} {get_tier_name(level)}",
                "lore": lore,
                "type": "ENTITY_KILL",
                "target-filter": target_filter,
                "target": target,
                "rewards": rewards_list
            }
            count += 1
    save_yaml('entity_kill.yml', achievements)

# 3. MINING
def gen_mining():
    achievements = {"achievements": {}}
    mining_data = [
        ("stone", "Đá", "STONE", [1000, 5000, 10000, 50000, 100000], 500),
        ("coal", "Than", "COAL_ORE", [100, 500, 1000, 5000], 500),
        ("deepslate_coal", "Than Đá Đen", "DEEPSLATE_COAL_ORE", [100, 500, 1000], 600),
        ("iron", "Sắt", "IRON_ORE", [100, 500, 1000, 5000], 1000),
        ("deepslate_iron", "Sắt Đá Đen", "DEEPSLATE_IRON_ORE", [100, 500, 1000, 5000], 1200),
        ("gold", "Vàng", "GOLD_ORE", [50, 200, 1000, 5000], 2000),
        ("deepslate_gold", "Vàng Đá Đen", "DEEPSLATE_GOLD_ORE", [50, 200, 1000, 5000], 2500),
        ("redstone", "Đá Đỏ", "REDSTONE_ORE", [100, 500, 2000, 10000], 1500),
        ("lapis", "Lưu Ly", "LAPIS_ORE", [100, 500, 2000, 10000], 1500),
        ("diamond", "Kim Cương", "DIAMOND_ORE", [10, 50, 200, 1000, 5000], 5000),
        ("deepslate_diamond", "Kim Cương Đá Đen", "DEEPSLATE_DIAMOND_ORE", [10, 50, 200, 1000], 6000),
        ("emerald", "Ngọc Lục Bảo", "EMERALD_ORE", [5, 20, 100, 500], 10000),
        ("netherrack", "Đá Địa Ngục", "NETHERRACK", [1000, 5000, 10000, 50000], 300),
        ("quartz", "Thạch Anh", "NETHER_QUARTZ_ORE", [100, 500, 2000], 1000),
        ("ancient_debris", "Mảnh Vỡ Cổ Đại", "ANCIENT_DEBRIS", [5, 20, 100], 15000),
        ("obsidian", "Hắc Diện Thạch", "OBSIDIAN", [50, 200, 1000], 3000),
    ]
    count = 0
    for id_prefix, name_prefix, target_filter, targets, base_reward in mining_data:
        for i, target in enumerate(targets):
            if count >= 50: break
            level = i + 1
            rewards_list = [f"eco give %player% {calculate_reward(base_reward, level, True)}"]
            lore = [f"&7Đào {target:,} khối {name_prefix}.", "", "&f🎁 &aPhần thưởng:"] + format_reward_lore(rewards_list)
            achievements["achievements"][f"miner_{id_prefix}_{level}"] = {
                "category": "mining",
                "display-name": f"&bThợ Mỏ {name_prefix} {get_tier_name(level)}",
                "lore": lore,
                "type": "BLOCK_BREAK",
                "target-filter": target_filter,
                "target": target,
                "rewards": rewards_list
            }
            count += 1
    save_yaml('mining.yml', achievements)

# 4. FARMING HARVEST
def gen_farming_harvest():
    achievements = {"achievements": {}}
    farm_data = [
        ("wheat_harvest", "Lúa", "WHEAT", [100, 500, 2000, 10000, 50000]),
        ("carrot_harvest", "Cà Rốt", "CARROTS", [100, 500, 2000, 10000, 50000]),
        ("potato_harvest", "Khoai Tây", "POTATOES", [100, 500, 2000, 10000, 50000]),
        ("pumpkin_harvest", "Bí Ngô", "PUMPKIN", [50, 200, 1000, 5000]),
        ("melon_harvest", "Dưa Hấu", "MELON", [50, 200, 1000, 5000]),
        ("sugar_cane_harvest", "Mía", "SUGAR_CANE", [100, 500, 2000, 10000]),
        ("beetroot_harvest", "Củ Cải Đường", "BEETROOTS", [100, 500, 2000, 10000]),
        ("cocoa_harvest", "Ca Cao", "COCOA", [50, 200, 1000]),
    ]
    count = 0
    for id_prefix, name_prefix, target_filter, targets in farm_data:
        for i, target in enumerate(targets):
            if count >= 50: break
            level = i + 1
            rewards_list = [f"eco give %player% {calculate_reward(200, level)}"]
            lore = [f"&7Thu hoạch {target:,} {name_prefix}.", "", "&f🎁 &aPhần thưởng:"] + format_reward_lore(rewards_list)
            achievements["achievements"][f"farmer_{id_prefix}_{level}"] = {
                "category": "farming_harvest",
                "display-name": f"&aThu Hoạch {name_prefix} {get_tier_name(level)}",
                "lore": lore,
                "type": "BLOCK_BREAK",
                "target-filter": target_filter,
                "target": target,
                "rewards": rewards_list
            }
            count += 1
    save_yaml('farming_harvest.yml', achievements)

# 5. FARMING PLANT
def gen_farming_plant():
    achievements = {"achievements": {}}
    farm_data = [
        ("wheat_plant", "Hạt Lúa", "WHEAT_SEEDS", [100, 500, 2000, 10000, 50000]),
        ("carrot_plant", "Cà Rốt", "CARROT", [100, 500, 2000, 10000, 50000]),
        ("potato_plant", "Khoai Tây", "POTATO", [100, 500, 2000, 10000, 50000]),
        ("oak_sapling", "Chồi Gỗ Sồi", "OAK_SAPLING", [50, 200, 1000]),
        ("birch_sapling", "Chồi Gỗ Bạch Dương", "BIRCH_SAPLING", [50, 200, 1000]),
        ("spruce_sapling", "Chồi Gỗ Vân Sam", "SPRUCE_SAPLING", [50, 200, 1000]),
        ("jungle_sapling", "Chồi Gỗ Rừng", "JUNGLE_SAPLING", [50, 200, 1000]),
        ("acacia_sapling", "Chồi Gỗ Xiêm Gai", "ACACIA_SAPLING", [50, 200, 1000]),
        ("dark_oak_sapling", "Chồi Gỗ Sồi Sẫm", "DARK_OAK_SAPLING", [50, 200, 1000]),
    ]
    count = 0
    for id_prefix, name_prefix, target_filter, targets in farm_data:
        for i, target in enumerate(targets):
            if count >= 50: break
            level = i + 1
            rewards_list = [f"eco give %player% {calculate_reward(200, level)}"]
            lore = [f"&7Trồng {target:,} {name_prefix}.", "", "&f🎁 &aPhần thưởng:"] + format_reward_lore(rewards_list)
            achievements["achievements"][f"farmer_{id_prefix}_{level}"] = {
                "category": "farming_plant",
                "display-name": f"&2Trồng {name_prefix} {get_tier_name(level)}",
                "lore": lore,
                "type": "BLOCK_PLACE",
                "target-filter": target_filter,
                "target": target,
                "rewards": rewards_list
            }
            count += 1
    save_yaml('farming_plant.yml', achievements)

# 6. PLAY TIME
def gen_play_time():
    achievements = {"achievements": {}}
    targets = generate_generic_targets(60, 30000)
    for i in range(50):
        level = i + 1
        target = targets[i]
        rewards_list = ["sololeveling forceattr %player% vitality 1"]
        
        if target >= 60:
            hours = target // 60
            mins = target % 60
            if mins > 0:
                time_str = f"{hours:,} giờ {mins} phút"
            else:
                time_str = f"{hours:,} giờ"
        else:
            time_str = f"{target:,} phút"

        lore = [f"#AAAAAA▶ #FFFFFFNhiệm vụ: #00E5FFChơi game {time_str}.", "", "#FFD700✦ Phần thưởng vinh dự:"] + format_reward_lore(rewards_list)
        achievements["achievements"][f"playtime_{level}"] = {
            "category": "play_time",
            "display-name": f"#00E5FF⌚ Người Chơi Chăm Chỉ #FFDD33★ {level}",
            "lore": lore,
            "type": "PLAY_TIME",
            "target": target,
            "rewards": rewards_list
        }
    save_yaml('play_time.yml', achievements)

# 7. CRATE OPEN
def gen_crate_open():
    achievements = {"achievements": {}}
    targets = generate_generic_targets(20, 10000)
    for i in range(50):
        level = i + 1
        target = targets[i]
        rewards_list = ["sololeveling forceattr %player% vitality 1"]
        lore = [f"#AAAAAA▶ #FFFFFFNhiệm vụ: #FFD700Mở {target:,} rương quay đồ ở Spawn.", "", "#FFD700✦ Phần thưởng vinh dự:"] + format_reward_lore(rewards_list)
        achievements["achievements"][f"crate_opener_{level}"] = {
            "category": "crate_open",
            "display-name": f"#FFD700🎁 Thợ Săn Rương #FFDD33★ {level}",
            "lore": lore,
            "type": "EXCELLENT_CRATE_OPEN",
            "target": target,
            "rewards": rewards_list
        }
    save_yaml('crate_open.yml', achievements)

# 8. POINTS EARN
def gen_points_earn():
    achievements = {"achievements": {}}
    targets = generate_generic_targets(20, 10000)
    for i in range(50):
        level = i + 1
        target = targets[i]
        rewards_list = ["sololeveling forceattr %player% vitality 1"]
        lore = [f"#AAAAAA▶ #FFFFFFNhiệm vụ: #FFAA00Kiếm được {target:,} Xu.", "", "#FFD700✦ Phần thưởng vinh dự:"] + format_reward_lore(rewards_list)
        achievements["achievements"][f"point_earner_{level}"] = {
            "category": "points_earn",
            "display-name": f"#FFAA00💰 Đại Gia Xu #FFDD33★ {level}",
            "lore": lore,
            "type": "PLAYER_POINTS_EARN",
            "target": target,
            "rewards": rewards_list
        }
    save_yaml('points_earn.yml', achievements)

# 9. POINTS SPEND
def gen_points_spend():
    achievements = {"achievements": {}}
    targets = generate_generic_targets(20, 10000)
    for i in range(50):
        level = i + 1
        target = targets[i]
        rewards_list = ["sololeveling forceattr %player% vitality 1"]
        lore = [f"#AAAAAA▶ #FFFFFFNhiệm vụ: #FF6600Tiêu {target:,} Xu.", "", "#FFD700✦ Phần thưởng vinh dự:"] + format_reward_lore(rewards_list)
        achievements["achievements"][f"point_spender_{level}"] = {
            "category": "points_spend",
            "display-name": f"#FF6600💸 Tiêu Xu Như Nước #FFDD33★ {level}",
            "lore": lore,
            "type": "PLAYER_POINTS_SPEND",
            "target": target,
            "rewards": rewards_list
        }
    save_yaml('points_spend.yml', achievements)

# 10. PLAYER JOIN
def gen_player_join():
    achievements = {"achievements": {}}
    targets = generate_generic_targets(20, 10000)
    for i in range(50):
        level = i + 1
        target = targets[i]
        rewards_list = ["sololeveling forceattr %player% vitality 1"]
        lore = [f"#AAAAAA▶ #FFFFFFNhiệm vụ: #4C99FFTham gia máy chủ {target:,} lần.", "", "#FFD700✦ Phần thưởng vinh dự:"] + format_reward_lore(rewards_list)
        achievements["achievements"][f"joiner_{level}"] = {
            "category": "player_join",
            "display-name": f"#4C99FF🧭 Khách Quen #FFDD33★ {level}",
            "lore": lore,
            "type": "PLAYER_JOIN",
            "target": target,
            "rewards": rewards_list
        }
    save_yaml('player_join.yml', achievements)

# 11. CHAT MESSAGE
def gen_chat_message():
    achievements = {"achievements": {}}
    targets = generate_generic_targets(20, 10000)
    for i in range(50):
        level = i + 1
        target = targets[i]
        rewards_list = ["sololeveling forceattr %player% vitality 1"]
        lore = [f"#AAAAAA▶ #FFFFFFNhiệm vụ: #FFFFFFGửi {target:,} tin nhắn.", "", "#FFD700✦ Phần thưởng vinh dự:"] + format_reward_lore(rewards_list)
        achievements["achievements"][f"chatter_{level}"] = {
            "category": "chat_message",
            "display-name": f"#FFFFFF💬 Thánh Chat #FFDD33★ {level}",
            "lore": lore,
            "type": "CHAT_MESSAGE",
            "target": target,
            "rewards": rewards_list
        }
    save_yaml('chat_message.yml', achievements)

# 12. COMMAND EXECUTE
def gen_command_execute():
    achievements = {"achievements": {}}
    targets = generate_generic_targets(20, 10000)
    for i in range(50):
        level = i + 1
        target = targets[i]
        rewards_list = ["sololeveling forceattr %player% vitality 1"]
        lore = [f"#AAAAAA▶ #FFFFFFNhiệm vụ: #AAAAAASử dụng lệnh {target:,} lần.", "", "#FFD700✦ Phần thưởng vinh dự:"] + format_reward_lore(rewards_list)
        achievements["achievements"][f"commander_{level}"] = {
            "category": "command_execute",
            "display-name": f"#AAAAAA⌨ Chuyên Gia Gõ Lệnh #FFDD33★ {level}",
            "lore": lore,
            "type": "COMMAND_EXECUTE",
            "target": target,
            "rewards": rewards_list
        }
    save_yaml('command_execute.yml', achievements)

def generate_gate_targets():
    targets = []
    for i in range(50):
        if i == 0:
            val = 100
        elif i == 49:
            val = 50000
        else:
            raw = 100 + (50000 - 100) * (i / 49.0)
            val = int(round(raw / 50.0) * 50)
        targets.append(val)
    return targets

def gen_gate(gate_id, gate_name, mob_ids, base_reward):
    achievements = {"achievements": {}}
    targets = generate_gate_targets()
    target_filter = ",".join(mob_ids)
    for i in range(50):
        level = i + 1
        target = targets[i]
        hp_reward = int(round(1 + i * (29.0 / 49)))
        rewards_list = [f"sololeveling forceattr %player% vitality {hp_reward}"]
        lore = [f"#AAAAAA▶ #FFFFFFNhiệm vụ: #B266FFTiêu diệt {target:,} quái vật Cổng {gate_name}.", "", "#FFD700✦ Phần thưởng vinh dự:"] + format_reward_lore(rewards_list)
        achievements["achievements"][f"gate_{gate_id.lower()}_{level}"] = {
            "category": f"gate_{gate_id.lower()}",
            "display-name": f"#B266FF⚔ Chinh Phục Cổng {gate_name} #FFDD33★ {get_tier_name(level)}",
            "lore": lore,
            "type": "MYTHIC_MOB_KILL",
            "target-filter": target_filter,
            "target": target,
            "rewards": rewards_list
        }
    save_yaml(f'gate_{gate_id.lower()}.yml', achievements)

def gen_rpg_gates():
    gate_e = ['RankE_OrcTanBinh', 'RankE_OrcThoSan', 'RankE_OrcPhuThuy', 'RankE_OrcVoSi']
    gate_d = ['RankD_orccungthu', 'RankD_orcchienbinh', 'RankD_orctute', 'RankD_orcdaudan']
    gate_c = ['RankC_hachiepsitanbinh', 'RankC_hachiepsicungthu', 'RankC_hachiepsitutruong', 'RankC_hachiepsichuy']
    gate_b = ['RankB_hachiepsitinhanh', 'RankB_hachiepsiamba', 'RankB_hachiepsitudo', 'RankB_hachiepsicuongsat']
    gate_a = ['RankA+banggia_bangcanve', 'RankA+banggia_hiep_si_bang', 'RankA+banggia_nguoi_tuyet_ac', 'RankA+banggia_bang_ma', 'RankA+daitute_giao_si', 'RankA+daitute_hiep_si_den', 'RankA+daitute_phap_su_cuong_tin', 'RankA+daitute_cuong_chien', 'RankA+hoanguc_hoaquy', 'RankA+hoanguc_acmalahanto', 'RankA+hoanguc_viemthu', 'RankA+hoanguc_chienbinhluadianguc', 'RankA+phongvu_loilinhcungthu', 'RankA+phongvu_phongvutula', 'RankA+phongvu_vanlongtu', 'RankA+phongvu_loithancunhan', 'RankA+phongvu_tinhlinhset', 'RankA+phongvu_thiencauthuatsi', 'RankA+phongvu_hacvanvugia', 'RankA+phongvu_phongvuong', 'RankA+phongvu_loivuong', 'RankA+thienduong_thiendoacanve', 'RankA+thienduong_thiendoathusan', 'RankA+thienduong_thiendoaphapsu', 'RankA+thienduong_thiendoahovekhonglo', 'RankA+thienduong_thiendoatute', 'RankA+thienduong_thiendoakysy', 'RankA+thienduong_thiendoadietthan', 'RankA+thienduong_thiendoadietvong', 'RankA+thienduong_thiendoanguyenrua', 'RankA_quai_vat_tinh_the_1', 'RankA_quai_vat_tinh_the_2', 'RankA_quai_vat_tinh_the_3', 'RankA_quai_vat_tinh_the_4']
    
    gen_gate("E", "E", gate_e, 500)
    gen_gate("D", "D", gate_d, 700)
    gen_gate("C", "C", gate_c, 1000)
    gen_gate("B", "B", gate_b, 1500)
    gen_gate("A", "A", gate_a, 2500)

def gen_boss_kill():
    achievements = {"achievements": {}}
    bosses = ['Orc_Giant', 'Orc_King', 'crowreaper', 'thecrowknight', 'apocalypse_type_a', 'koboldWizard_em', 'ice_golem', 'Mage', 'LR_HYDRA']
    targets = generate_generic_targets(10, 10000, 30)
    target_filter = ",".join(bosses)
    base_reward = 3000
    for i in range(30):
        level = i + 1
        target = targets[i]
        hp_reward = i + 1
        rewards_list = [f"sololeveling forceattr %player% vitality {hp_reward}"]
        lore = [f"#AAAAAA▶ #FFFFFFNhiệm vụ: #FF3333Tiêu diệt {target:,} tất cả Boss ở Dungeon.", "", "#FFD700✦ Phần thưởng vinh dự:", f"  &8▪ &c+{hp_reward} Điểm chỉ số Máu"]
        achievements["achievements"][f"boss_kill_{level}"] = {
            "category": "boss_kill",
            "display-name": f"#FF3333🐉 Thợ Săn Boss #FFDD33★ {get_tier_name(level)}",
            "lore": lore,
            "type": "MYTHIC_MOB_KILL",
            "target-filter": target_filter,
            "target": target,
            "rewards": rewards_list
        }
    save_yaml('boss_kill.yml', achievements)

# 13. SOLO LEVEL
def gen_solo_level():
    achievements = {"achievements": {}}
    targets = generate_generic_targets(50, 900, 18)
    for i in range(18):
        level = i + 1
        target = targets[i]
        hp_reward = int(round(1 + i * (49.0 / 17)))
        rewards_list = [f"sololeveling forceattr %player% vitality {hp_reward}"]
        lore = [f"#AAAAAA▶ #FFFFFFNhiệm vụ: #55FF55Đạt Cấp Độ {target:,}.", "", "#FFD700✦ Phần thưởng vinh dự:", f"  &8▪ &c+{hp_reward} Điểm chỉ số Máu"]
        achievements["achievements"][f"solo_level_{level}"] = {
            "category": "solo_level",
            "display-name": f"#55FF55🌟 Thăng Cấp #FFDD33★ {level}",
            "lore": lore,
            "type": "SOLO_LEVEL_REACH",
            "target": target,
            "rewards": rewards_list
        }
    save_yaml('solo_level.yml', achievements)

# 14. SOLO RANK
def gen_solo_rank():
    achievements = {"achievements": {}}
    ranks = [
        ("rank_d", "Hạng D", "#A9A9A9"),
        ("rank_c", "Hạng C", "#55FF55"),
        ("rank_b", "Hạng B", "#00AAFF"),
        ("rank_a", "Hạng A", "#AA00AA"),
        ("rank_s", "Hạng S", "#FFAA00")
    ]
    for i, (rank_id, rank_name, color) in enumerate(ranks):
        level = i + 1
        hp_reward = (i + 1) * 30
        rewards_list = [f"sololeveling forceattr %player% vitality {hp_reward}"]
        lore = [f"#AAAAAA▶ #FFFFFFNhiệm vụ: {color}Tái Thức Tỉnh lên {rank_name}.", "", "#FFD700✦ Phần thưởng vinh dự:", f"  &8▪ &c+{hp_reward} Điểm chỉ số Máu"]
        achievements["achievements"][f"solo_rank_{rank_id}"] = {
            "category": "solo_rank",
            "display-name": f"{color}👑 Chuyển Sinh {rank_name}",
            "lore": lore,
            "type": "SOLO_RANK_REACH",
            "target-filter": rank_id,
            "target": 1,
            "rewards": rewards_list
        }
    save_yaml('solo_rank.yml', achievements)

gen_player_kill()
gen_play_time()
gen_crate_open()
gen_points_earn()
gen_points_spend()
gen_player_join()
gen_chat_message()
gen_command_execute()
gen_rpg_gates()
gen_boss_kill()
gen_solo_level()
gen_solo_rank()
