#!/bin/bash

RES=(1080x960 960x800 1920x1280 1440x1280 2160x1920 2880x2560)
RES_NAME=(hdpi nodpi sw600dp-nodpi xhdpi xxhdpi xxxhdpi)

declare -A resolutions

resolutions[hdpi]=1080x960
resolutions[nodpi]=960x800
resolutions[sw600dp-nodpi]=1920x1280
resolutions[xhdpi]=1440x1280
resolutions[xxhdpi]=2160x1920
resolutions[xxxhdpi]=2880x2560

rm -rf wallpaper-res/drawable*
rm -rf res/values/wallpapers.xml

for wallpaper in wallpapers/*; do
    echo $wallpaper
    wallpaper_name=`basename $wallpaper`
    wallpaper_name=${wallpaper_name//-/_}
    wallpaper_name=`echo $wallpaper_name | cut -d'.' -f1`
    resolution="${resolutions[@]}"
    for n in "${!resolutions[@]}"; do
        resolution="${resolutions[$n]}"
        if [ ! -d wallpaper-res/drawable-$n ]; then
            mkdir -p wallpaper-res/drawable-$n
        fi
        convert $wallpaper -resize $resolution png24:wallpaper-res/drawable-$n/$wallpaper_name.png
    done
    convert $wallpaper -resize 213x178 png24:wallpaper-res/drawable-nodpi/"$wallpaper_name"_small.png
done

cat <<'EOF' >> res/values/wallpapers.xml
<?xml version="1.0" encoding="utf-8"?>
<!--
     Copyright (C) 2016 The SlimRoms Project
     Licensed under the Apache License, Version 2.0 (the "License");
     you may not use this file except in compliance with the License.
     You may obtain a copy of the License at
          http://www.apache.org/licenses/LICENSE-2.0
     Unless required by applicable law or agreed to in writing, software
     distributed under the License is distributed on an "AS IS" BASIS,
     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     See the License for the specific language governing permissions and
     limitations under the License.
-->
<resources xmlns:xliff="urn:oasis:names:tc:xliff:document:1.2">
    <string-array name="wallpapers" translatable="false">
EOF

for wall in wallpapers/*; do
    name=`basename $wall | cut -d'.' -f1`
    name=${name//-/_}
    echo "        <item>$name</item>" >> res/values/wallpapers.xml
done

echo "    </string-array>" >> res/values/wallpapers.xml
echo "</resources>" >> res/values/wallpapers.xml
