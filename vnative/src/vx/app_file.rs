struct Application {
    magic: [u8; 4], // "VRLL"
    version: u8,    // 4...5...6...7 as Virtel versions
    string_table: Vec<String>,
    functions: Vec<SerializedFunction>,
}
struct SerializedFunction {
    pub signature: FunctionSignature,
    pub result: Option<FunctionResult>,
    pub instructions: Vec<u8>,
}
