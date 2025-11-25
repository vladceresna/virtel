use slotmap::{Key, SlotMap};

#[derive(Clone, Copy, Debug, PartialEq)]
pub enum Cell {
    I64(i64),
    F64(f64),
    U64(u64),
    Bool(bool),
    StrRef(Key),
    ArrayRef(Key),
    FuncRef(Key),
    Null,
}
#[derive(Serialize, Deserialize)]
pub enum Constant {
    I64(i64),
    F64(f64),
    U64(u64),
    Bool(bool),
    String(String),
    Array(SlotMap<Key, Cell>),
    Function(Function),
}
#[derive(Debug, Clone, Serialize, Deserialize, Encode, Decode)]
pub struct Function {
    pub args: Vec<Cell>,
    pub result_type: Cell,
    pub instructions: Vec<u8>,
}

#[cfg(test)]
mod tests {
    use crate::app::Op;

    use super::*;
    #[test]
    fn it_works() {
        let signature = Function {
            args: vec![Cell::FuncRef(()), Cell::F64(())],
            result_type: Cell::Bool(()),
            instructions: vec![Op::LoadConst as u8],
        };

        let result = add(2, 2);
        assert_eq!(result, 4);
    }
}
