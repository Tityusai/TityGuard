# TityGuard

Paper 1.21.x icin bagimsiz anti-cheat. Vulcan ile cakismaz (ayri komut /tg, ayri permission, ayri config).

## Derleme
Java 21 + Maven gerekir:

    mvn clean package

Jar: `target/TityGuard-1.0.0.jar` -> sunucunun `plugins/` klasorune at.

## Vulcan ile birlikte calistirma
Ayni anda ikisini calistirirsan ikisi de setback atip oyuncuyu bozabilir.
Test icin config.yml'de `settings.setback: false` ve `settings.punish: false` yap (sadece alert).

## Permissionlar
- tityguard.command / tityguard.alerts / tityguard.admin (varsayilan: op)
- tityguard.bypass (varsayilan: kimse)

## Kapsam (v1)
Speed, Fly (yercekimi tahmini), NoFall, Timer, Reach (+duvar arkasi), KillAura (nisan/hitbox + multi-target),
AutoClicker (CPS + tutarlilik), Scaffold/FastPlace, FastBreak/Nuker.

## Bilinen eksikler / yol haritasi
Paket seviyesi (PacketEvents) olmadigi icin: Velocity/AntiKB, BadPackets, rotasyon GCD analizi, kesin ping-spoof tespiti yok.
Esikler (config.yml) kendi sunucunda test edilerek ayarlanmali.
