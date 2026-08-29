import json
import sys

t = open(sys.argv[1], encoding='utf-8').read()
print('parameters occurrences:', t.count('"parameters"'))
print('addSurfaceBiome occurrences:', t.count('"addSurfaceBiome"'))
j = json.loads(t)
mnb = j['mappings']['pub/pigeon/yggdyy/hexmob/mixin/MultiNoiseBiomeSourceMixin']
owb = j['mappings']['pub/pigeon/yggdyy/hexmob/mixin/OverworldBiomeBuilderMixin']
print('MNB keys:', list(mnb.keys()))
print('MNB getNoiseBiome value:', mnb.get('getNoiseBiome(IIILnet/minecraft/world/level/biome/Climate$Sampler;)Lnet/minecraft/core/Holder;'))
print('MNB parameters:', mnb.get('parameters'))
print('OWB keys:', list(owb.keys()))
print('OWB addSurfaceBiome:', owb.get('addSurfaceBiome'))