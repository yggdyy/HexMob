# patch-refmap.ps1
# 修复 Loom refmap 无法为以下两种情形生成映射的问题（1.20.1 已实测）：
#   1) 重载方法名 getNoiseBiome（类层级里有 3 个同名的）→ Loom 不映射方法名，发布包(intermediary)找不到目标崩启动
#   2) 签名里含 Climate$... 嵌套类型的 @Shadow 方法（parameters / addSurfaceBiome）→ Loom 完全不生成映射
# 用法: powershell -File scripts\patch-refmap.ps1 -Jar <hexmob-fabric-*.jar>
# 实现: 解包 -> python fix_refmap.py 结构化补丁（幂等、防重复键）-> jar 重打包覆盖原 jar。
param([Parameter(Mandatory=$true)][string]$Jar)

$ErrorActionPreference = "Stop"
$jarTool = "C:\Program Files\Java\jdk-17\bin\jar.exe"
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$entryName = "hexmob-common.refmap.json"
$tmp = Join-Path $env:TEMP ("hexmob-refmap-" + [guid]::NewGuid().ToString("N"))

try {
    # 1) 解包
    New-Item -ItemType Directory -Force -Path $tmp | Out-Null
    Push-Location $tmp
    & $jarTool xf $Jar
    if ($LASTEXITCODE -ne 0) { throw "jar 解包失败（zip 可能已损坏）: $Jar" }
    Pop-Location

    # 2) python 结构化补丁
    & python (Join-Path $scriptDir "fix_refmap.py") (Join-Path $tmp $entryName) (Join-Path $tmp $entryName)
    if ($LASTEXITCODE -ne 0) { throw "fix_refmap 失败" }

    # 3) jar 重打包覆盖（保留 META-INF/MANIFEST，目录条目由 jar 工具正确写出）
    $repacked = $Jar + ".new"
    Push-Location $tmp
    & $jarTool cfM $repacked -C $tmp .
    $code = $LASTEXITCODE
    Pop-Location
    if ($code -ne 0) { throw "jar 重打包失败" }
    Move-Item -Force $repacked $Jar

    # 4) 打印当前 mixin 映射确认
    $j = Get-Content (Join-Path $tmp $entryName) -Raw | ConvertFrom-Json
    Write-Output "--- MultiNoiseBiomeSourceMixin ---"
    $j.mappings.'pub/pigeon/yggdyy/hexmob/mixin/MultiNoiseBiomeSourceMixin' | ConvertTo-Json
    Write-Output "--- OverworldBiomeBuilderMixin ---"
    $j.mappings.'pub/pigeon/yggdyy/hexmob/mixin/OverworldBiomeBuilderMixin' | ConvertTo-Json
}
finally {
    if (Test-Path $tmp) { Remove-Item $tmp -Recurse -Force -ErrorAction SilentlyContinue }
}