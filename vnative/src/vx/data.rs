pub enum NewCell {
    I64(i64),
    F64(f64),
    U64(u64),
    Bool(bool),
    StrRef(Index),
    ArrayRef(Index),
    FuncRef(Index),
    Null,
}
