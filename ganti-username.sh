#!/usr/bin/env bash
# Pemakaian: ./ganti-username.sh abdullah_ubaid   (jalankan dari folder proyek)
set -e
NEW="$1"; [ -z "$NEW" ] && { echo "Isi username: ./ganti-username.sh <username>"; exit 1; }
grep -rl "username" --include=*.java --include=*.xml --include=*.yaml --include=*.md . | xargs sed -i "s/username/${NEW}/g"
for d in src/main/java/org/delcom src/test/java/org/delcom; do
  [ -d "$d/usernamepbo2026springbootcashflow" ] && mv "$d/usernamepbo2026springbootcashflow" "$d/${NEW}pbo2026springbootcashflow"
done
cd .. && mv username-pbo2026-springboot-cashflow "${NEW}-pbo2026-springboot-cashflow"
echo "Selesai. Folder baru: ${NEW}-pbo2026-springboot-cashflow"
