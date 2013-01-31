package template;

public enum Type {
	Unknown,
	
	Block,
	BlockClose,
	EndBlock,
	
	Function,
	FunctionClose,
	
	Simple,
	Identifier,
	Text,
	
	Root
}
