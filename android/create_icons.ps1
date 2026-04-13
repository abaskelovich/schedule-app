Add-Type -AssemblyName System.Drawing

$sizes = @{
    "mipmap-mdpi" = 48
    "mipmap-hdpi" = 72
    "mipmap-xhdpi" = 96
    "mipmap-xxhdpi" = 144
    "mipmap-xxxhdpi" = 192
}

$color = [System.Drawing.Color]::FromArgb(255, 98, 0, 238)

foreach ($dir in $sizes.Keys) {
    $size = $sizes[$dir]
    $bmp = New-Object System.Drawing.Bitmap($size, $size)
    $g = [System.Drawing.Graphics]::FromImage($bmp)
    $g.Clear($color)
    $g.Dispose()
    
    $path = "C:\Users\User\Projects\Schedule\android\app\src\main\res\$dir\ic_launcher.png"
    $bmp.Save($path, [System.Drawing.Imaging.ImageFormat]::Png)
    $bmp.Dispose()
    
    $pathRound = "C:\Users\User\Projects\Schedule\android\app\src\main\res\$dir\ic_launcher_round.png"
    $bmp2 = New-Object System.Drawing.Bitmap($size, $size)
    $g2 = [System.Drawing.Graphics]::FromImage($bmp2)
    $g2.Clear($color)
    $g2.Dispose()
    $bmp2.Save($pathRound, [System.Drawing.Imaging.ImageFormat]::Png)
    $bmp2.Dispose()
}

Write-Host "Icons created successfully"
