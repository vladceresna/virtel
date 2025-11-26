use std::sync::Arc;

use bincode::{Decode, Encode};
use serde::{Deserialize, Serialize};
use slotmap::{DefaultKey, KeyData, SlotMap};

use crate::app::{VMError, VMResult};

#[derive(Clone, Debug, PartialEq, Serialize, Deserialize, Encode, Decode)]
pub enum CellType {
    I64,
    F64,
    U64,
    Bool,
    StrRef,
    ArrayRef,
    FuncRef(FunctionSignature),
    Null,
}
#[derive(Clone, Copy, Debug, PartialEq, Serialize, Deserialize, Encode, Decode)]
pub enum Cell {
    I64(i64),
    F64(f64),
    U64(u64),
    Bool(bool),
    StrRef(u64),
    ArrayRef(u64),
    FuncRef(u64),
    Null,
}
impl Cell {
    pub fn to_key(&self) -> VMResult<DefaultKey> {
        let value = match self {
            Cell::StrRef(value) => value,
            Cell::ArrayRef(value) => value,
            Cell::FuncRef(value) => value,
            _ => {
                return Err(VMError::NativeReferencesCastingError(
                    "It is not supporting type Cell".to_string(),
                ))
            }
        };
        let def_key: DefaultKey = KeyData::from_ffi(value.clone()).into();
        Ok(def_key)
    }
}
#[derive(Serialize, Deserialize)]
pub enum Constant {
    I64(i64),
    F64(f64),
    U64(u64),
    Bool(bool),
    String(String),
    Array(SlotMap<DefaultKey, Cell>),
    Function(Function),
}
#[derive(Clone, Debug, PartialEq, Serialize, Deserialize, Encode, Decode)]
pub struct Function {
    pub signature: FunctionSignature,
    pub instructions: Vec<u8>,
}
#[derive(Clone, Debug, PartialEq, Serialize, Deserialize, Encode, Decode)]
pub struct FunctionSignature {
    pub args: Vec<CellType>,
    pub result_type: Box<CellType>,
}

#[cfg(test)]
mod tests {
    use crate::app::Op;

    use super::*;
    #[test]
    fn it_works() {
        let signature = Function {
            signature: FunctionSignature {
                args: vec![
                    CellType::FuncRef(FunctionSignature {
                        args: vec![],
                        result_type: CellType::Null,
                    }),
                    CellType::F64,
                ],
                result_type: CellType::Null,
            },
            instructions: vec![Op::LoadConst as u8],
        };

        assert_eq!(4, 4);
    }
}
