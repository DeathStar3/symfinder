public abstract class TreeDecorator<T extends ChristmasTree> implements ChristmasTree {
    private T tree;

    public TreeDecorator(T tree) {
        this.tree = tree;
    }

    @Override
    public String decorate() {
        return tree.decorate();
    }
}