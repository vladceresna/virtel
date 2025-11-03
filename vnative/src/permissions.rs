use std::collections::HashSet;

#[derive(Clone, Copy, PartialEq, Eq, Hash, Debug)]
pub enum Capability {
    FileWrite,
    Network,
    Crypto,
}

pub struct Capabilities {
    allowed: HashSet<Capability>,
}

impl Capabilities {
    pub fn new() -> Self {
        Self { allowed: HashSet::new() }
    }

    pub fn grant(&mut self, cap: Capability) {
        self.allowed.insert(cap);
    }

    pub fn check(&self, cap: Capability) -> bool {
        self.allowed.contains(&cap)
    }
}
