# Virtel File Execution Docs
## AppFile
- Magic: 4 bytes: VXCL
- Virtel Version: 1 byte: 4
- Entry points: Vec bytes for functions
- Global Consts
## Data in App
1. App
- Global Consts: `Vec<Constant>`
2. Thread
- Output: `Arc<RwLock<Vec<Constant>>>`
- Private Data: `Vec<Constant>`
3. Function Frame
- Registers: `[Cell;256]`

### Commands
LoadConst (clone object from Global Consts to Private Data and Register)
ExportConst (pub idx) ()
ImportConst
