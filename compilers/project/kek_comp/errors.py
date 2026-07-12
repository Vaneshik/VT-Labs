class KekError(Exception):
    pass


class LexerError(KekError):
    pass


class ParseError(KekError):
    pass


class RuntimeKekError(KekError):
    pass
