use std::collections::HashSet;

#[derive(Clone, Copy, PartialEq, Eq, Hash, Debug)]
pub enum Permission {
    Files,
    Network,
}
pub struct Permissions {
    allowed: HashSet<Permission>,
}
impl Permissions {
    pub fn new() -> Self {
        Self {
            allowed: HashSet::new(),
        }
    }
    pub fn grant(&mut self, cap: Permission) {
        self.allowed.insert(cap);
    }
    pub fn check(&self, cap: Permission) -> bool {
        self.allowed.contains(&cap)
    }
}
