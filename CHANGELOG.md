# Changelog

## 1.0.2.1
- Replaced the Money Bag fallback for large purse death penalties with standard Numismatic coin drops.
- Preserved Numismatic's one-coin-per-item-entity drop behaviour and existing currency totals.
- Replaced repeated console output with structured logging. Ordinary transaction details are debug-only; errors remain visible.
- Kept true-death detection, gamerule calculations, purse update ordering and transaction safeguards unchanged.
- Updated release version and licence metadata.

## 1.0.2
- Fixed physical currency appearing at the respawn point instead of the true death position.
- Moved penalty handling to Fabric's confirmed true-death event, excluding Hardcore Revival knockouts and successful rescues.
- Used native player item drops and retained rollback safeguards around purse deduction.

## 1.0.1
- Replaced runtime reflection with typed Minecraft, Fabric and Numismatic Overhaul APIs.
- Improved currency transaction rollback and development dependency management.

## 1.0.0
- Initial compatibility fix and source baseline.
