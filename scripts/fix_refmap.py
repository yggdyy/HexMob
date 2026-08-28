# -*- coding: utf-8 -*-
# HexMob refmap 修复（JSON 结构化，幂等）：
#   1) MultiNoiseBiomeSourceMixin.getNoiseBiome 值里的方法名 -> method_38109
#   2) 补 parameters（@Shadow 字段）-> method_49506
#   3) 补 OverworldBiomeBuilderMixin.addSurfaceBiome -> method_38187
# 实现：json.loads 读取（重复 mixin 键取后者 = 有效映射），dict 定点修改后重新 dump。
# 用法：python fix_refmap.py <in.json> <out.json>
import json
import sys

inp, out = sys.argv[1], sys.argv[2]
t = open(inp, encoding="utf-8").read()
j = json.loads(t)  # 重复键默认取后者（与 GSON 读取一致）

m = j.setdefault("mappings", {})
mnb = "pub/pigeon/yggdyy/hexmob/mixin/MultiNoiseBiomeSourceMixin"
owb = "pub/pigeon/yggdyy/hexmob/mixin/OverworldBiomeBuilderMixin"

changed = False

if mnb in m and isinstance(m[mnb], dict):
    nmap = {}
    for k, v in m[mnb].items():
        if isinstance(v, str):
            v = v.replace("class_4766;getNoiseBiome(", "class_4766;method_38109(")
        nmap[k] = v
    if not any("method_49506" in str(v) for v in nmap.values()):
        nmap["parameters"] = "Lnet/minecraft/class_4766;method_49506()Lnet/minecraft/class_6544$class_6547;"
        changed = True
    m[mnb] = nmap

if owb in m and isinstance(m[owb], dict):
    if not any("method_38187" in str(v) for v in m[owb].values()):
        m[owb]["addSurfaceBiome"] = (
            "Lnet/minecraft/class_6554;method_38187("
            "Lnet/minecraft/class_6544$class_6546;"
            "Lnet/minecraft/class_6544$class_6546;"
            "Lnet/minecraft/class_6544$class_6546;"
            "Lnet/minecraft/class_6544$class_6546;"
            "Lnet/minecraft/class_6544$class_6546;FLacp;)V"
        )
        changed = True

# 校验：不允许重复键
danger = json.dumps(j, ensure_ascii=False)
for key in ("\"parameters\"", "\"addSurfaceBiome\""):
    n = danger.count(key)
    if n > 1:
        raise SystemExit("FAIL: duplicate key %s (%d)" % (key, n))

open(out, "w", encoding="utf-8").write(json.dumps(j, indent=2, ensure_ascii=False))
print("PATCHED ok" if changed else "NO CHANGE")