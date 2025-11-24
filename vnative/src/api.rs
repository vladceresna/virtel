use generational_arena::Index;

pub struct Api {
    pub functions: Vec<usize>,
}
pub struct FunctionSignature {
    pub args: Vec<Cell>,
}
pub struct Function {
    pub signature: FunctionSignature,
    pub instructions: Vec<u8>,
}
