# GlowPayment

GlowPayment is a modern, high-performance currency transfer and PvP economy plugin for Minecraft (Paper, Folia, Purpur).

## Features

- **Player Money Transfers (`/pay`)**: Smooth and secure currency transactions using Vault economy.
- **Configurable Transfer Tax**: Deduct an adjustable percentage from transfers to balance server economy.
- **PvP Balance Loss**: Configurable percentage of victim's balance deducted upon death in PvP.
- **Reward or Burn**: Choose whether PvP penalty is rewarded to the killer or removed from the economy.
- **Robust Security**: Safe decimal handling with `BigDecimal`, self-transfer prevention, limit bounds, and negative amount validation.
- **MiniMessage Support**: Rich text styling with gradient, hex colors, and placeholders.
- **Full Folia & Paper Compatibility**: Built with modern Paper/Folia standards.

## Commands

- `/pay <player> <amount>` — Send money to another player. (Alias: `/transfer`)
- `/glowpayment reload` — Reload configuration and messages. (Alias: `/gp reload`)

## Permissions

- `glowpayment.use` — Permission to use `/pay`. (Default: true)
- `glowpayment.admin` — Permission to reload plugin settings. (Default: op)
- `glowpayment.bypass.tax` — Bypass transfer commission. (Default: op)
- `glowpayment.bypass.pvploss` — Bypass balance deduction on PvP death. (Default: op)

## Build

```bash
mvn clean package
```
