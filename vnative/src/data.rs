use bincode::{Decode, Encode};
use serde::{Deserialize, Serialize};

use crate::app::{push_to_local_heap, VMError, VMResult};

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
    pub fn to_key(&self) -> VMResult<u64> {
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
        Ok(*value)
    }
}
#[derive(Debug, PartialEq, Clone, Serialize, Deserialize, Encode, Decode)]
pub enum Constant {
    I64(i64),
    F64(f64),
    U64(u64),
    Bool(bool),
    String(String),
    Array(Vec<Cell>),
    Function(Function),
    Null,
}
fn constant_to_cell(constant: &Constant, local_heap: &mut Vec<Constant>) -> VMResult<Cell> {
    Ok(match constant {
        Constant::I64(v) => Cell::I64(*v),
        Constant::F64(v) => Cell::F64(*v),
        Constant::U64(v) => Cell::U64(*v),
        Constant::Bool(v) => Cell::Bool(*v),
        Constant::String(s) => {
            let idx = push_to_local_heap(local_heap, constant.clone());
            Cell::StrRef(idx)
        }
        Constant::Array(arr) => {
            let idx = push_to_local_heap(local_heap, constant.clone());
            Cell::ArrayRef(idx)
        }
        Constant::Function(f) => {
            let idx = push_to_local_heap(local_heap, constant.clone());
            Cell::FuncRef(idx)
        }
        _ => Cell::Null,
    })
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
        let function = Function {
            signature: FunctionSignature {
                args: vec![
                    CellType::FuncRef(FunctionSignature {
                        args: vec![],
                        result_type: Box::new(CellType::Null),
                    }),
                    CellType::F64,
                ],
                result_type: Box::new(CellType::Null),
            },
            instructions: vec![Op::LoadConst as u8],
        };

        assert_eq!(4, 4);
    }
}
