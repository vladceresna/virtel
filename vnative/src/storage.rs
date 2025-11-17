type SimpleValue = u64;
type ValueList = Vec<SimpleValue>;

struct Storage {
    // 0 - registers array
    // 1 - strings array
    // 2 - objects array
    heap: Vec<ValueList>,
}
