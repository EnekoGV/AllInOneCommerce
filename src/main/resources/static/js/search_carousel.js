const prevs  = document.querySelector('.prevs');
const nexts = document.querySelector('.nexts');

const track = document.querySelector('.track');

let carouselWidth = document.querySelector('.carousel-container').offsetWidth;

window.addEventListener('resize', () => {
    carouselWidth = document.querySelector('.carousel-container').offsetWidth;
})

let index = 0;

nexts.addEventListener('click', () => {
    index++;
    prevs.classList.add('showt');
    track.style.transform = `translateX(-${index * carouselWidth}px)`;

    if (track.offsetWidth - (index * carouselWidth) < carouselWidth) {
        nexts.classList.add('hidet');
    }
})

prevs.addEventListener('click', () => {
    index--;
    nexts.classList.remove('hidet');
    if (index === 0) {
        prevs.classList.remove('showt');
    }
    track.style.transform = `translateX(-${index * carouselWidth}px)`;
})
