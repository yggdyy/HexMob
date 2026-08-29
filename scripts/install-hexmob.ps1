# 安装 hexmob fabric jar 到整合包 mods 目录（带完整性校验）。
# 用法：.\scripts\install-hexmob.ps1 -Jar <path-to-built-jar> [-ModsDir <path>]
# 流程：校验源 jar 可解析 → patch-refmap（散射 mixin 需要）→ Copy-Item → SHA256 对比源/目标。
param(
    [Parameter(Mandatory = $true)]
    [string]$Jar,

    [string]$ModsDir = "D:\rd\casting\.minecraft\versions\小咩的咒法学大包\mods"
)

$ErrorActionPreference = "Stop"
$jarTool = "C:\Program Files\Java\jdk-17\bin\jar.exe"
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path

if (-not (Test-Path $Jar)) { throw "源 jar 不存在: $Jar" }

# 1) 源 jar 完整性：能列出条目才算可解析
& $jarTool tf $Jar | Out-Null
if ($LASTEXITCODE -ne 0) { throw "源 jar 损坏（zip 无法解析）: $Jar" }

# 2) 打 refmap 补丁（散射 mixin 的 intermediary 映射；补丁会就地改写 jar）
Push-Location (Split-Path -Parent $scriptDir)
& ".\scripts\patch-refmap.ps1" -Jar $Jar | Out-Null
if ($LASTEXITCODE -ne 0) { throw "patch-refmap 失败" }
Pop-Location

# 3) 复制到 mods 目录（文件名沿用 hexmob-fabric-...jar）
$name = [System.IO.Path]::GetFileName($Jar)
$dest = Join-Path $ModsDir $name
Copy-Item -LiteralPath $Jar -Destination $dest -Force

# 4) 校验：SHA256 一致 + 目标可解析
$srcHash = (Get-FileHash -LiteralPath $Jar -Algorithm SHA256).Hash
$dstHash = (Get-FileHash -LiteralPath $dest -Algorithm SHA256).Hash
if ($srcHash -ne $dstHash) { throw "安装后校验失败：源/目标 SHA256 不一致！$name" }
& $jarTool tf $dest | Out-Null
if ($LASTEXITCODE -ne 0) { throw "安装后校验失败：目标 jar 无法解析！$name" }

Write-Output "installed+verified: $name ($((Get-Item $dest).Length) bytes, SHA256=$dstHash)"