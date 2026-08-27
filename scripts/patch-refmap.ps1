# patch-refmap.ps1
# 修复 Loom refmap 无法为以下两种情形生成映射的问题（1.20.1 已实测）：
#   1) 重载方法名 getNoiseBiome（类层级里有 3 个同名的）→ Loom 不映射方法名，发布包(intermediary)找不到目标崩启动
#   2) 签名里含 Climate$... 嵌套类型的 @Shadow 方法（parameters / addSurfaceBiome）→ Loom 完全不生成映射
# 用法: powershell -File scripts\patch-refmap.ps1 -Jar <hexmob-fabric-*.jar>
param([Parameter(Mandatory=$true)][string]$Jar)

$ErrorActionPreference = "Stop"
Add-Type -AssemblyName System.IO.Compression.FileSystem

$entryName = "hexmob-common.refmap.json"
$zip = [System.IO.Compression.ZipFile]::Open($Jar, "Update")
try {
    $e = $zip.GetEntry($entryName)
    if ($null -eq $e) { throw "refmap entry not found: $entryName" }
    $r = New-Object System.IO.StreamReader($e.Open())
    $text = $r.ReadToEnd()
    $r.Dispose()
    $orig = $text

    # --- 1) 修正 getNoiseBiome 的方法名映射: getNoiseBiome -> method_38109 (2 处: mappings + data.named:intermediary)
    $text = $text.Replace(
        'class_4766;getNoiseBiome(IIILnet/minecraft/class_6544$class_6552;)Lnet/minecraft/class_6880;',
        'class_4766;method_38109(IIILnet/minecraft/class_6544$class_6552;)Lnet/minecraft/class_6880;')

    # --- 2) 补 MultiNoiseBiomeSourceMixin.parameters @Shadow -> method_49506 (2 处)
    $text = $text.Replace(
        '"Lnet/minecraft/class_4766;method_38109(IIILnet/minecraft/class_6544$class_6552;)Lnet/minecraft/class_6880;"',
        '"Lnet/minecraft/class_4766;method_38109(IIILnet/minecraft/class_6544$class_6552;)Lnet/minecraft/class_6880;",' +
        "`n      `"parameters`": `"Lnet/minecraft/class_4766;method_49506()Lnet/minecraft/class_6544`$class_6547;`"")

    # --- 3) 补 OverworldBiomeBuilderMixin.addSurfaceBiome @Shadow -> method_38187 (2 处)
    $text = $text.Replace(
        '"addBiomes": "Lnet/minecraft/class_6554;method_38185(Ljava/util/function/Consumer;)V"',
        '"addBiomes": "Lnet/minecraft/class_6554;method_38185(Ljava/util/function/Consumer;)V",' +
        "`n      `"addSurfaceBiome`": `"Lnet/minecraft/class_6554;method_38187(Lnet/minecraft/class_6544`$class_6546;Lnet/minecraft/class_6544`$class_6546;Lnet/minecraft/class_6544`$class_6546;Lnet/minecraft/class_6544`$class_6546;Lnet/minecraft/class_6544`$class_6546;FLacp;)V`"")

    if ($text -eq $orig) {
        Write-Output "NO CHANGE (already patched or nothing matched)"
    } else {
        # 校验补丁后仍是合法 JSON
        $null = $text | ConvertFrom-Json
        # 写回 zip
        $e.Delete()
        $ne = $zip.CreateEntry($entryName)
        $w = New-Object System.IO.StreamWriter($ne.Open())
        $w.Write($text)
        $w.Dispose()
        Write-Output "PATCHED ok"
    }

    # 打印当前 mixin 映射确认
    $check = $zip.GetEntry($entryName)
    if ($check) {
        $rr = New-Object System.IO.StreamReader($check.Open())
        $j = $rr.ReadToEnd() | ConvertFrom-Json
        $rr.Dispose()
        Write-Output "--- MultiNoiseBiomeSourceMixin ---"
        $j.mappings.'pub/pigeon/yggdyy/hexmob/mixin/MultiNoiseBiomeSourceMixin' | ConvertTo-Json
        Write-Output "--- OverworldBiomeBuilderMixin ---"
        $j.mappings.'pub/pigeon/yggdyy/hexmob/mixin/OverworldBiomeBuilderMixin' | ConvertTo-Json
    }
}
finally {
    $zip.Dispose()
}
